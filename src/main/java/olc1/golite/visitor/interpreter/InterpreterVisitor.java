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
import olc1.golite.visitor.interpreter.value.*;

public class InterpreterVisitor implements Visitor<ValueWrapper> {

    public String output = "";
    private final ValueWrapper defaultVoid = new VoidValue(-1, -1);
    private Environment currentEnv = new Environment(null); // ambito global

    public ValueWrapper Visit(ASTNode node) {
        return node.accept(this);
    }

    // ---------- Helpers de error y tipos ----------
    private SemanticException semantic(String msg, ValueWrapper at) {
        return new SemanticException(new GoliteError("Semantico", msg, at.line(), at.column()));
    }

    private SemanticException semantic(String msg, int line, int column) {
        return new SemanticException(new GoliteError("Semantico", msg, line, column));
    }

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

    // Verifica asignabilidad a 'declared'; aplica conversion implicita int -> float64
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

    // ---------- Literales ----------
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
        // El lexema viene con comillas, las quitamos para el contenido real
        String raw = ctx.value;
        String content = (raw.length() >= 2) ? raw.substring(1, raw.length() - 1) : raw;
        return new StringValue(content, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(Paren.Context ctx) {
        return Visit(ctx.expression);
    }

    // ---------- Aritmetica ----------
    @Override
    public ValueWrapper visit(Add.Context ctx) {
        ValueWrapper left = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue l when right instanceof IntValue r ->
                new IntValue(l.value() + r.value(), l.line(), l.column());
            case IntValue l when right instanceof DecimalValue r ->
                new DecimalValue(l.value() + r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue r ->
                new DecimalValue(l.value() + r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r ->
                new DecimalValue(l.value() + r.value(), l.line(), l.column());
            case StringValue l when right instanceof StringValue r ->
                new StringValue(l.value() + r.value(), l.line(), l.column());
            default ->
                throw semantic("Operacion invalida: " + left.getTypeName() + " + " + right.getTypeName(), left);
        };
    }

    @Override
    public ValueWrapper visit(Sub.Context ctx) {
        ValueWrapper left = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue l when right instanceof IntValue r ->
                new IntValue(l.value() - r.value(), l.line(), l.column());
            case IntValue l when right instanceof DecimalValue r ->
                new DecimalValue(l.value() - r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue r ->
                new DecimalValue(l.value() - r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r ->
                new DecimalValue(l.value() - r.value(), l.line(), l.column());
            default ->
                throw semantic("Operacion invalida: " + left.getTypeName() + " - " + right.getTypeName(), left);
        };
    }

    @Override
    public ValueWrapper visit(Mul.Context ctx) {
        ValueWrapper left = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue l when right instanceof IntValue r ->
                new IntValue(l.value() * r.value(), l.line(), l.column());
            case IntValue l when right instanceof DecimalValue r ->
                new DecimalValue(l.value() * r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue r ->
                new DecimalValue(l.value() * r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r ->
                new DecimalValue(l.value() * r.value(), l.line(), l.column());
            default ->
                throw semantic("Operacion invalida: " + left.getTypeName() + " * " + right.getTypeName(), left);
        };
    }

    @Override
    public ValueWrapper visit(Div.Context ctx) {
        ValueWrapper left = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue l when right instanceof IntValue r ->
                new IntValue(l.value() / r.value(), l.line(), l.column());
            case IntValue l when right instanceof DecimalValue r ->
                new DecimalValue(l.value() / r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue r ->
                new DecimalValue(l.value() / r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r ->
                new DecimalValue(l.value() / r.value(), l.line(), l.column());
            default ->
                throw semantic("Operacion invalida: " + left.getTypeName() + " / " + right.getTypeName(), left);
        };
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

    // ---------- Variables ----------
    @Override
    public ValueWrapper visit(VarDecl.Context ctx) {
        ValueWrapper value;
        GoliteType declared = ctx.declaredType;

        if (ctx.value != null) {
            ValueWrapper evaluated = Visit(ctx.value);
            if (declared == null) {
                // x := e   -> inferir el tipo del valor
                declared = typeOf(evaluated);
                value = evaluated;
            } else {
                // var x T = e   -> validar y (si aplica) convertir
                value = checkAndConvert(declared, evaluated, ctx.line, ctx.column);
            }
        } else {
            // var x T   -> valor por defecto del tipo
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
        SymbolEntry entry = currentEnv.get(ctx.name, ctx.line, ctx.column); // error si no esta declarada
        ValueWrapper converted = checkAndConvert(entry.getType(), value, ctx.line, ctx.column);
        entry.setValue(converted);
        return defaultVoid;
    }

    // ---------- Sentencias ----------
    @Override
    public ValueWrapper visit(Imprimir.Context ctx) {
        ValueWrapper value = Visit(ctx.expression);
        output += value.toString() + "\n";
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(IfNode.Context ctx) {
        ValueWrapper cond = Visit(ctx.condition);
        if (cond instanceof BoolValue b && b.value()) {
            Visit(ctx.body);
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(Statments.Context ctx) {
        for (ASTNode statment : ctx.statements) {
            Visit(statment);
        }
        return defaultVoid;
    }
}
