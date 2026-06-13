package olc1.golite.visitor.interpreter;

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
import olc1.golite.visitor.interpreter.value.*;

public class InterpreterVisitor implements Visitor<ValueWrapper> {

    public String output = "";
    private final ValueWrapper defaultVoid = new VoidValue(-1, -1);
    private Environment currentEnv = new Environment(null); // ambito global

    public ValueWrapper Visit(ASTNode node) {
        return node.accept(this);
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

    // numerico = int o float64
    private boolean esNumerico(ValueWrapper v) {
        return v instanceof IntValue || v instanceof DecimalValue;
    }

    // pasamos cualquier numerico a double para operar/comparar mezclando int y float64
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

    // ===== Aritmetica (en helpers para reusarla en +=, -= e i++) =====
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
            // las cadenas se concatenan con +
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
        // revisamos division entre cero antes de operar
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
        // el modulo solo aplica entre enteros
        if (l instanceof IntValue a && r instanceof IntValue b) {
            if (b.value() == 0) {
                throw semantic("No se puede aplicar modulo entre cero", r);
            }
            return new IntValue(a.value() % b.value(), a.line(), a.column());
        }
        throw semantic("Operacion invalida: " + l.getTypeName() + " % " + r.getTypeName(), l);
    }

    // ===== Comparaciones =====
    // igualdad: numericos (mezclando int/float), bool, string y rune
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

    // orden (< <= > >=): solo numericos y runes (los runes por su valor ASCII)
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
    // procesa las secuencias de escape dentro de una cadena
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
                i++; // ya consumimos el caracter que seguia a la barra
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // convierte el lexema de un rune ('A', '\n', ...) en su codigo de caracter
    private int parseRune(String lexeme) {
        String inner = lexeme.substring(1, lexeme.length() - 1); // quitamos las comillas simples
        if (inner.length() == 1) {
            return inner.charAt(0);
        }
        char esc = inner.charAt(1); // es un escape como \n
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
        // el lexema viene con comillas dobles; las quitamos y procesamos los escapes
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

    // ===== Variables =====
    @Override
    public ValueWrapper visit(VarDecl.Context ctx) {
        ValueWrapper value;
        GoliteType declared = ctx.declaredType;

        if (ctx.value != null) {
            ValueWrapper evaluated = Visit(ctx.value);
            if (declared == null) {
                // x := e  -> el tipo se infiere del valor
                declared = typeOf(evaluated);
                value = evaluated;
            } else {
                // var x T = e  -> validamos y convertimos si aplica
                value = checkAndConvert(declared, evaluated, ctx.line, ctx.column);
            }
        } else {
            // var x T  -> toma el valor por defecto del tipo
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

        // += es como variable = variable + expr, y -= igual con la resta
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

        // i++ equivale a i = i + 1
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
                    // continue: seguimos al post y la siguiente iteracion
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
    public ValueWrapper visit(Statments.Context ctx) {
        for (ASTNode statment : ctx.statements) {
            Visit(statment);
        }
        return defaultVoid;
    }
}
