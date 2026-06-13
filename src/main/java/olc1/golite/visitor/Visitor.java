package olc1.golite.visitor;

import olc1.golite.ast.exp.*;
import olc1.golite.ast.stm.*;

public interface Visitor<T> {

    // Literales y agrupacion
    T visit(Integers.Context ctx);

    T visit(Decimal.Context ctx);

    T visit(BoolLiteral.Context ctx);

    T visit(StringLiteral.Context ctx);

    T visit(RuneLiteral.Context ctx);

    T visit(Paren.Context ctx);

    // Aritmetica
    T visit(Add.Context ctx);

    T visit(Sub.Context ctx);

    T visit(Mul.Context ctx);

    T visit(Div.Context ctx);

    T visit(Mod.Context ctx);

    T visit(Negate.Context ctx);

    // Comparaciones
    T visit(Equal.Context ctx);

    T visit(NotEqual.Context ctx);

    T visit(Greater.Context ctx);

    T visit(GreaterEqual.Context ctx);

    T visit(Less.Context ctx);

    T visit(LessEqual.Context ctx);

    // Logicos
    T visit(And.Context ctx);

    T visit(Or.Context ctx);

    T visit(Not.Context ctx);

    // Variables
    T visit(VarRef.Context ctx);

    T visit(VarDecl.Context ctx);

    T visit(Assign.Context ctx);

    T visit(CompoundAssign.Context ctx);

    // Sentencias
    T visit(Imprimir.Context ctx);

    T visit(IfNode.Context ctx);

    T visit(Statments.Context ctx);
}
