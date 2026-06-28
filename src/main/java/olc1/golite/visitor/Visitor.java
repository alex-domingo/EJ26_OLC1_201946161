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

    T visit(SliceLiteral.Context ctx);

    T visit(NestedSlice.Context ctx);

    T visit(StructLiteral.Context ctx);

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

    // Embebidas que devuelven valor
    T visit(Atoi.Context ctx);

    T visit(ParseFloat.Context ctx);

    T visit(TypeOf.Context ctx);

    T visit(Index.Context ctx);

    T visit(FieldAccess.Context ctx);

    T visit(Len.Context ctx);

    T visit(Append.Context ctx);

    T visit(SliceIndex.Context ctx);

    T visit(StringsJoin.Context ctx);

    // Llamada a funcion
    T visit(Call.Context ctx);

    T visit(MethodCall.Context ctx);

    // Variables
    T visit(VarRef.Context ctx);

    T visit(VarDecl.Context ctx);

    T visit(Assign.Context ctx);

    T visit(IndexAssign.Context ctx);

    T visit(FieldAssign.Context ctx);

    T visit(CompoundAssign.Context ctx);

    T visit(IncDec.Context ctx);

    // Sentencias
    T visit(Println.Context ctx);

    T visit(IfNode.Context ctx);

    T visit(ForNode.Context ctx);

    T visit(ForRange.Context ctx);

    T visit(SwitchNode.Context ctx);

    T visit(Bloque.Context ctx);

    T visit(BreakNode.Context ctx);

    T visit(ContinueNode.Context ctx);

    T visit(ReturnNode.Context ctx);

    T visit(FuncDecl.Context ctx);

    T visit(MethodDecl.Context ctx);

    T visit(StructDecl.Context ctx);

    T visit(Statments.Context ctx);
}
