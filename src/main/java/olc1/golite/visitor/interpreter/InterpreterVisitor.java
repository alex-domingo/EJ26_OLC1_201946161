package olc1.golite.visitor.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import olc1.golite.ast.ASTNode;
import olc1.golite.ast.exp.*;
import olc1.golite.ast.stm.*;
import olc1.golite.reports.GoliteError;
import olc1.golite.reports.SemanticException;
import olc1.golite.symbols.Environment;
import olc1.golite.symbols.GoliteType;
import olc1.golite.symbols.SymbolEntry;
import olc1.golite.visitor.Visitor;
import olc1.golite.visitor.interpreter.control.BreakSignal;
import olc1.golite.visitor.interpreter.control.ContinueSignal;
import olc1.golite.visitor.interpreter.control.ReturnSignal;
import olc1.golite.visitor.interpreter.value.*;

public class InterpreterVisitor implements Visitor<ValueWrapper> {

    public String output = "";
    private final ValueWrapper defaultVoid = new VoidValue(-1, -1);

    // ambito global (raiz de la cadena de ambitos)
    private final Environment globalEnv = new Environment(null);
    // ambito actual durante la ejecucion
    private Environment currentEnv = globalEnv;
    // tabla de funciones declaradas en el ambito global
    private final Map<String, FuncDecl> functions = new HashMap<>();

    public ValueWrapper Visit(ASTNode node) {
        return node.accept(this);
    }

    // ===== Punto de entrada del interprete =====
    public void interpret(ASTNode root) {
        List<ASTNode> top;
        if (root instanceof Statments s) {
            top = s.getStatements();
        } else {
            top = new ArrayList<>();
            top.add(root);
        }

        // Pasada 1: registramos (hoisting) todas las funciones, asi se puede
        // llamar a una funcion declarada mas abajo en el archivo
        for (ASTNode node : top) {
            if (node instanceof FuncDecl fd) {
                registerFunction(fd);
            }
        }

        // Pasada 2: ejecutamos las sentencias del nivel global (variables globales
        // y sentencias sueltas), saltando las declaraciones de funcion
        for (ASTNode node : top) {
            if (!(node instanceof FuncDecl)) {
                Visit(node);
            }
        }

        // si existe una funcion main, es el punto de entrada
        if (functions.containsKey("main")) {
            callFunction("main", new ArrayList<>(), -1, -1);
        }
    }

    private void registerFunction(FuncDecl fd) {
        // no pueden existir dos funciones con el mismo nombre
        if (functions.containsKey(fd.getName())) {
            throw semantic("La funcion '" + fd.getName() + "' ya fue declarada", fd.getLine(), fd.getColumn());
        }
        // los parametros no pueden repetir nombre
        Set<String> vistos = new HashSet<>();
        for (Param p : fd.getParams()) {
            if (!vistos.add(p.name)) {
                throw semantic("El parametro '" + p.name + "' esta repetido en la funcion '" + fd.getName() + "'",
                        p.line, p.column);
            }
        }
        functions.put(fd.getName(), fd);
    }

    // ejecuta una funcion: crea su ambito, vincula parametros, corre el cuerpo y maneja el return
    private ValueWrapper callFunction(String name, List<ValueWrapper> argValues, int line, int column) {
        FuncDecl fn = functions.get(name);
        if (fn == null) {
            throw semantic("La funcion '" + name + "' no esta definida", line, column);
        }
        if (argValues.size() != fn.getParams().size()) {
            throw semantic("La funcion '" + name + "' esperaba " + fn.getParams().size()
                    + " argumento(s) pero recibio " + argValues.size(), line, column);
        }

        // el ambito de la funcion cuelga del GLOBAL, no del llamador
        Environment fnEnv = new Environment(globalEnv);
        for (int i = 0; i < fn.getParams().size(); i++) {
            Param p = fn.getParams().get(i);
            ValueWrapper arg = checkAndConvert(p.type, argValues.get(i), p.line, p.column);
            fnEnv.declare(new SymbolEntry(p.name, p.type, arg, p.line, p.column));
        }

        Environment prev = currentEnv;
        currentEnv = fnEnv;
        boolean returned = false;
        ValueWrapper result = defaultVoid;
        try {
            if (fn.getBody() != null) {
                Visit(fn.getBody());
            }
        } catch (ReturnSignal rs) {
            returned = true;
            if (fn.getReturnType() == null) {
                // funcion sin tipo de retorno: no puede devolver un valor
                if (rs.hasValue()) {
                    throw semantic("La funcion '" + name + "' no declara tipo de retorno y no puede retornar un valor",
                            rs.getLine(), rs.getColumn());
                }
            } else {
                if (!rs.hasValue()) {
                    throw semantic("La funcion '" + name + "' debe retornar un valor de tipo " + fn.getReturnType().getLabel(),
                            rs.getLine(), rs.getColumn());
                }
                result = checkAndConvert(fn.getReturnType(), rs.getValue(), rs.getLine(), rs.getColumn());
            }
        } finally {
            currentEnv = prev;
        }

        // funcion con tipo de retorno que termino sin ejecutar un return
        if (!returned && fn.getReturnType() != null) {
            throw semantic("La funcion '" + name + "' debe retornar un valor de tipo " + fn.getReturnType().getLabel(),
                    line, column);
        }
        return result;
    }

    // ===== Helpers de error =====
    private SemanticException semantic(String msg, ValueWrapper at) {
        return new SemanticException(new GoliteError("Semantico", msg, at.line(), at.column()));
    }

    private SemanticException semantic(String msg, int line, int column) {
        return new SemanticException(new GoliteError("Semantico", msg, line, column));
    }

    // ===== Helpers de tipos =====
    private GoliteType typeOf(ValueWrapper v) {
        return switch (v) {
            case IntValue x ->
                GoliteType.INT;
            case DecimalValue x ->
                GoliteType.FLOAT64;
            case StringValue x ->
                GoliteType.STRING;
            case BoolValue x ->
                GoliteType.BOOL;
            case RuneValue x ->
                GoliteType.RUNE;
            case VoidValue x ->
                GoliteType.VOID;
        };
    }

    private boolean esNumerico(ValueWrapper v) {
        return v instanceof IntValue || v instanceof DecimalValue;
    }

    private double aDouble(ValueWrapper v) {
        if (v instanceof IntValue i) {
            return i.value();
        }
        return ((DecimalValue) v).value();
    }

    private ValueWrapper defaultValue(GoliteType type, int line, int column) {
        return switch (type) {
            case INT ->
                new IntValue(0, line, column);
            case FLOAT64 ->
                new DecimalValue(0.0, line, column);
            case STRING ->
                new StringValue("", line, column);
            case BOOL ->
                new BoolValue(false, line, column);
            case RUNE ->
                new RuneValue(0, line, column);
            case VOID ->
                defaultVoid;
        };
    }

    // valida asignabilidad a 'declared'. unica conversion implicita: int -> float64
    private ValueWrapper checkAndConvert(GoliteType declared, ValueWrapper value, int line, int column) {
        GoliteType actual = typeOf(value);
        if (actual == declared) {
            return value;
        }
        if (declared == GoliteType.FLOAT64 && actual == GoliteType.INT) {
            IntValue iv = (IntValue) value;
            return new DecimalValue(iv.value(), line, column);
        }
        throw semantic("No se puede asignar un valor de tipo " + actual.getLabel()
                + " a una variable de tipo " + declared.getLabel(), line, column);
    }

    // ===== Aritmetica (helpers reusados en +=, -= e i++) =====
    private ValueWrapper sumar(ValueWrapper l, ValueWrapper r) {
        return switch (l) {
            case IntValue a when r instanceof IntValue b ->
                new IntValue(a.value() + b.value(), a.line(), a.column());
            case IntValue a when r instanceof DecimalValue b ->
                new DecimalValue(a.value() + b.value(), a.line(), a.column());
            case DecimalValue a when r instanceof IntValue b ->
                new DecimalValue(a.value() + b.value(), a.line(), a.column());
            case DecimalValue a when r instanceof DecimalValue b ->
                new DecimalValue(a.value() + b.value(), a.line(), a.column());
            case StringValue a when r instanceof StringValue b ->
                new StringValue(a.value() + b.value(), a.line(), a.column());
            default ->
                throw semantic("Operacion invalida: " + l.getTypeName() + " + " + r.getTypeName(), l);
        };
    }

    private ValueWrapper restar(ValueWrapper l, ValueWrapper r) {
        return switch (l) {
            case IntValue a when r instanceof IntValue b ->
                new IntValue(a.value() - b.value(), a.line(), a.column());
            case IntValue a when r instanceof DecimalValue b ->
                new DecimalValue(a.value() - b.value(), a.line(), a.column());
            case DecimalValue a when r instanceof IntValue b ->
                new DecimalValue(a.value() - b.value(), a.line(), a.column());
            case DecimalValue a when r instanceof DecimalValue b ->
                new DecimalValue(a.value() - b.value(), a.line(), a.column());
            default ->
                throw semantic("Operacion invalida: " + l.getTypeName() + " - " + r.getTypeName(), l);
        };
    }

    private ValueWrapper multiplicar(ValueWrapper l, ValueWrapper r) {
        return switch (l) {
            case IntValue a when r instanceof IntValue b ->
                new IntValue(a.value() * b.value(), a.line(), a.column());
            case IntValue a when r instanceof DecimalValue b ->
                new DecimalValue(a.value() * b.value(), a.line(), a.column());
            case DecimalValue a when r instanceof IntValue b ->
                new DecimalValue(a.value() * b.value(), a.line(), a.column());
            case DecimalValue a when r instanceof DecimalValue b ->
                new DecimalValue(a.value() * b.value(), a.line(), a.column());
            default ->
                throw semantic("Operacion invalida: " + l.getTypeName() + " * " + r.getTypeName(), l);
        };
    }

    private ValueWrapper dividir(ValueWrapper l, ValueWrapper r) {
        if (esNumerico(r) && aDouble(r) == 0.0) {
            throw semantic("No se puede dividir entre cero", r);
        }
        return switch (l) {
            case IntValue a when r instanceof IntValue b ->
                new IntValue(a.value() / b.value(), a.line(), a.column());
            case IntValue a when r instanceof DecimalValue b ->
                new DecimalValue(a.value() / b.value(), a.line(), a.column());
            case DecimalValue a when r instanceof IntValue b ->
                new DecimalValue(a.value() / b.value(), a.line(), a.column());
            case DecimalValue a when r instanceof DecimalValue b ->
                new DecimalValue(a.value() / b.value(), a.line(), a.column());
            default ->
                throw semantic("Operacion invalida: " + l.getTypeName() + " / " + r.getTypeName(), l);
        };
    }

    private ValueWrapper modulo(ValueWrapper l, ValueWrapper r) {
        if (l instanceof IntValue a && r instanceof IntValue b) {
            if (b.value() == 0) {
                throw semantic("No se puede aplicar modulo entre cero", r);
            }
            return new IntValue(a.value() % b.value(), a.line(), a.column());
        }
        throw semantic("Operacion invalida: " + l.getTypeName() + " % " + r.getTypeName(), l);
    }

    // ===== Comparaciones =====
    private boolean sonIguales(ValueWrapper a, ValueWrapper b) {
        if (esNumerico(a) && esNumerico(b)) {
            return aDouble(a) == aDouble(b);
        }
        if (a instanceof BoolValue x && b instanceof BoolValue y) {
            return x.value() == y.value();
        }
        if (a instanceof StringValue x && b instanceof StringValue y) {
            return x.value().equals(y.value());
        }
        if (a instanceof RuneValue x && b instanceof RuneValue y) {
            return x.value() == y.value();
        }
        throw semantic("No se pueden comparar los tipos " + a.getTypeName() + " y " + b.getTypeName(), a);
    }

    private double compararOrden(ValueWrapper a, ValueWrapper b) {
        if (esNumerico(a) && esNumerico(b)) {
            return aDouble(a) - aDouble(b);
        }
        if (a instanceof RuneValue x && b instanceof RuneValue y) {
            return x.value() - y.value();
        }
        throw semantic("No se puede comparar el orden entre " + a.getTypeName() + " y " + b.getTypeName(), a);
    }

    // ===== Helpers de strings y runes =====
    private String procesarEscapes(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char sig = s.charAt(i + 1);
                switch (sig) {
                    case 'n' ->
                        sb.append('\n');
                    case 'r' ->
                        sb.append('\r');
                    case 't' ->
                        sb.append('\t');
                    case '"' ->
                        sb.append('"');
                    case '\\' ->
                        sb.append('\\');
                    default -> {
                        sb.append('\\');
                        sb.append(sig);
                    }
                }
                i++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private int parseRune(String lexeme) {
        String inner = lexeme.substring(1, lexeme.length() - 1);
        if (inner.length() == 1) {
            return inner.charAt(0);
        }
        char esc = inner.charAt(1);
        return switch (esc) {
            case 'n' ->
                '\n';
            case 'r' ->
                '\r';
            case 't' ->
                '\t';
            case '\\' ->
                '\\';
            case '\'' ->
                '\'';
            default ->
                esc;
        };
    }

    // ===== Literales =====
    @Override
    public ValueWrapper visit(Integers.Context ctx) {
        return new IntValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(Decimal.Context ctx) {
        return new DecimalValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(BoolLiteral.Context ctx) {
        return new BoolValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(StringLiteral.Context ctx) {
        String raw = ctx.value;
        String content = (raw.length() >= 2) ? raw.substring(1, raw.length() - 1) : raw;
        return new StringValue(procesarEscapes(content), ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(RuneLiteral.Context ctx) {
        return new RuneValue(parseRune(ctx.lexeme), ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(Paren.Context ctx) {
        return Visit(ctx.expression);
    }

    // ===== Aritmetica =====
    @Override
    public ValueWrapper visit(Add.Context ctx) {
        return sumar(Visit(ctx.left), Visit(ctx.right));
    }

    @Override
    public ValueWrapper visit(Sub.Context ctx) {
        return restar(Visit(ctx.left), Visit(ctx.right));
    }

    @Override
    public ValueWrapper visit(Mul.Context ctx) {
        return multiplicar(Visit(ctx.left), Visit(ctx.right));
    }

    @Override
    public ValueWrapper visit(Div.Context ctx) {
        return dividir(Visit(ctx.left), Visit(ctx.right));
    }

    @Override
    public ValueWrapper visit(Mod.Context ctx) {
        return modulo(Visit(ctx.left), Visit(ctx.right));
    }

    @Override
    public ValueWrapper visit(Negate.Context ctx) {
        ValueWrapper operand = Visit(ctx.expression);
        return switch (operand) {
            case IntValue v ->
                new IntValue(-v.value(), v.line(), v.column());
            case DecimalValue v ->
                new DecimalValue(-v.value(), v.line(), v.column());
            default ->
                throw semantic("Operacion invalida: -" + operand.getTypeName(), operand);
        };
    }

    // ===== Comparaciones (devuelven bool) =====
    @Override
    public ValueWrapper visit(Equal.Context ctx) {
        ValueWrapper l = Visit(ctx.left);
        ValueWrapper r = Visit(ctx.right);
        return new BoolValue(sonIguales(l, r), l.line(), l.column());
    }

    @Override
    public ValueWrapper visit(NotEqual.Context ctx) {
        ValueWrapper l = Visit(ctx.left);
        ValueWrapper r = Visit(ctx.right);
        return new BoolValue(!sonIguales(l, r), l.line(), l.column());
    }

    @Override
    public ValueWrapper visit(Greater.Context ctx) {
        ValueWrapper l = Visit(ctx.left);
        ValueWrapper r = Visit(ctx.right);
        return new BoolValue(compararOrden(l, r) > 0, l.line(), l.column());
    }

    @Override
    public ValueWrapper visit(GreaterEqual.Context ctx) {
        ValueWrapper l = Visit(ctx.left);
        ValueWrapper r = Visit(ctx.right);
        return new BoolValue(compararOrden(l, r) >= 0, l.line(), l.column());
    }

    @Override
    public ValueWrapper visit(Less.Context ctx) {
        ValueWrapper l = Visit(ctx.left);
        ValueWrapper r = Visit(ctx.right);
        return new BoolValue(compararOrden(l, r) < 0, l.line(), l.column());
    }

    @Override
    public ValueWrapper visit(LessEqual.Context ctx) {
        ValueWrapper l = Visit(ctx.left);
        ValueWrapper r = Visit(ctx.right);
        return new BoolValue(compararOrden(l, r) <= 0, l.line(), l.column());
    }

    // ===== Logicos (ambos lados deben ser bool) =====
    @Override
    public ValueWrapper visit(And.Context ctx) {
        ValueWrapper l = Visit(ctx.left);
        ValueWrapper r = Visit(ctx.right);
        if (l instanceof BoolValue a && r instanceof BoolValue b) {
            return new BoolValue(a.value() && b.value(), a.line(), a.column());
        }
        throw semantic("El operador && requiere expresiones booleanas", l);
    }

    @Override
    public ValueWrapper visit(Or.Context ctx) {
        ValueWrapper l = Visit(ctx.left);
        ValueWrapper r = Visit(ctx.right);
        if (l instanceof BoolValue a && r instanceof BoolValue b) {
            return new BoolValue(a.value() || b.value(), a.line(), a.column());
        }
        throw semantic("El operador || requiere expresiones booleanas", l);
    }

    @Override
    public ValueWrapper visit(Not.Context ctx) {
        ValueWrapper v = Visit(ctx.expression);
        if (v instanceof BoolValue b) {
            return new BoolValue(!b.value(), b.line(), b.column());
        }
        throw semantic("El operador ! requiere una expresion booleana", v);
    }

    // ===== Embebidas =====
    @Override
    public ValueWrapper visit(Atoi.Context ctx) {
        ValueWrapper v = Visit(ctx.expression);
        if (!(v instanceof StringValue s)) {
            throw semantic("strconv.Atoi requiere una cadena", ctx.line, ctx.column);
        }
        try {
            return new IntValue(Integer.parseInt(s.value().trim()), ctx.line, ctx.column);
        } catch (NumberFormatException ex) {
            throw semantic("No se puede convertir a int la cadena: \"" + s.value() + "\"", ctx.line, ctx.column);
        }
    }

    @Override
    public ValueWrapper visit(ParseFloat.Context ctx) {
        ValueWrapper v = Visit(ctx.expression);
        if (!(v instanceof StringValue s)) {
            throw semantic("strconv.ParseFloat requiere una cadena", ctx.line, ctx.column);
        }
        try {
            return new DecimalValue(Double.parseDouble(s.value().trim()), ctx.line, ctx.column);
        } catch (NumberFormatException ex) {
            throw semantic("No se puede convertir a float64 la cadena: \"" + s.value() + "\"", ctx.line, ctx.column);
        }
    }

    @Override
    public ValueWrapper visit(TypeOf.Context ctx) {
        ValueWrapper v = Visit(ctx.expression);
        return new StringValue(typeOf(v).getLabel(), ctx.line, ctx.column);
    }

    // ===== Llamada a funcion =====
    @Override
    public ValueWrapper visit(Call.Context ctx) {
        // los argumentos se evaluan en el ambito del llamador
        List<ValueWrapper> args = new ArrayList<>();
        for (ASTNode a : ctx.args) {
            args.add(Visit(a));
        }
        return callFunction(ctx.name, args, ctx.line, ctx.column);
    }

    // ===== Variables =====
    @Override
    public ValueWrapper visit(VarDecl.Context ctx) {
        // en el ambito global una variable no puede llamarse igual que una funcion
        if (currentEnv == globalEnv && functions.containsKey(ctx.name)) {
            throw semantic("Ya existe una funcion con el nombre '" + ctx.name + "'", ctx.line, ctx.column);
        }

        ValueWrapper value;
        GoliteType declared = ctx.declaredType;

        if (ctx.value != null) {
            ValueWrapper evaluated = Visit(ctx.value);
            if (declared == null) {
                declared = typeOf(evaluated);
                value = evaluated;
            } else {
                value = checkAndConvert(declared, evaluated, ctx.line, ctx.column);
            }
        } else {
            value = defaultValue(declared, ctx.line, ctx.column);
        }

        currentEnv.declare(new SymbolEntry(ctx.name, declared, value, ctx.line, ctx.column));
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(VarRef.Context ctx) {
        SymbolEntry entry = currentEnv.get(ctx.name, ctx.line, ctx.column);
        return entry.getValue();
    }

    @Override
    public ValueWrapper visit(Assign.Context ctx) {
        ValueWrapper value = Visit(ctx.value);
        SymbolEntry entry = currentEnv.get(ctx.name, ctx.line, ctx.column);
        ValueWrapper converted = checkAndConvert(entry.getType(), value, ctx.line, ctx.column);
        entry.setValue(converted);
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(CompoundAssign.Context ctx) {
        SymbolEntry entry = currentEnv.get(ctx.name, ctx.line, ctx.column);
        ValueWrapper actual = entry.getValue();
        ValueWrapper rhs = Visit(ctx.value);
        ValueWrapper resultado = (ctx.op == '+') ? sumar(actual, rhs) : restar(actual, rhs);
        ValueWrapper convertido = checkAndConvert(entry.getType(), resultado, ctx.line, ctx.column);
        entry.setValue(convertido);
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(IncDec.Context ctx) {
        SymbolEntry entry = currentEnv.get(ctx.name, ctx.line, ctx.column);
        ValueWrapper actual = entry.getValue();
        ValueWrapper uno = new IntValue(1, ctx.line, ctx.column);
        ValueWrapper resultado = (ctx.op == '+') ? sumar(actual, uno) : restar(actual, uno);
        ValueWrapper convertido = checkAndConvert(entry.getType(), resultado, ctx.line, ctx.column);
        entry.setValue(convertido);
        return defaultVoid;
    }

    // ===== Sentencias =====
    @Override
    public ValueWrapper visit(Println.Context ctx) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ctx.args.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(Visit(ctx.args.get(i)).toString());
        }
        output += sb.toString() + "\n";
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(IfNode.Context ctx) {
        ValueWrapper cond = Visit(ctx.condition);
        if (!(cond instanceof BoolValue)) {
            throw semantic("La condicion del if debe ser de tipo bool", cond);
        }
        if (((BoolValue) cond).value()) {
            Visit(ctx.body);
        } else if (ctx.elseBranch != null) {
            Visit(ctx.elseBranch);
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(ForNode.Context ctx) {
        Environment previo = currentEnv;
        currentEnv = new Environment(previo);
        try {
            if (ctx.init != null) {
                Visit(ctx.init);
            }
            while (true) {
                ValueWrapper cond = Visit(ctx.condition);
                if (!(cond instanceof BoolValue)) {
                    throw semantic("La condicion del for debe ser de tipo bool", cond);
                }
                if (!((BoolValue) cond).value()) {
                    break;
                }
                try {
                    Visit(ctx.body);
                } catch (ContinueSignal cs) {
                    // continue: pasamos al post y la siguiente iteracion
                } catch (BreakSignal bs) {
                    break;
                }
                if (ctx.post != null) {
                    Visit(ctx.post);
                }
            }
        } finally {
            currentEnv = previo;
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(SwitchNode.Context ctx) {
        ValueWrapper sv = Visit(ctx.expression);
        try {
            for (CaseClause c : ctx.cases) {
                ValueWrapper cv = Visit(c.value);
                if (sonIguales(sv, cv)) {
                    ejecutarCuerpoCase(c.body);
                    return defaultVoid; // break implicito: no hay fall-through
                }
            }
            if (ctx.defaultBody != null) {
                ejecutarCuerpoCase(ctx.defaultBody);
            }
        } catch (BreakSignal bs) {
            // un break dentro del switch simplemente lo termina
        }
        return defaultVoid;
    }

    private void ejecutarCuerpoCase(ASTNode body) {
        if (body == null) {
            return;
        }
        Environment previo = currentEnv;
        currentEnv = new Environment(previo);
        try {
            Visit(body);
        } finally {
            currentEnv = previo;
        }
    }

    @Override
    public ValueWrapper visit(Bloque.Context ctx) {
        Environment previo = currentEnv;
        currentEnv = new Environment(previo);
        try {
            if (ctx.body != null) {
                Visit(ctx.body);
            }
        } finally {
            currentEnv = previo;
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(BreakNode.Context ctx) {
        throw new BreakSignal(ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(ContinueNode.Context ctx) {
        throw new ContinueSignal(ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(ReturnNode.Context ctx) {
        ValueWrapper v = (ctx.expression != null) ? Visit(ctx.expression) : null;
        throw new ReturnSignal(v, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(FuncDecl.Context ctx) {
        // si llegamos aqui ejecutando, la funcion esta anidada (no en el global)
        throw semantic("Las funciones solo pueden declararse en el ambito global", ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(Statments.Context ctx) {
        for (ASTNode statment : ctx.statements) {
            Visit(statment);
        }
        return defaultVoid;
    }
}
