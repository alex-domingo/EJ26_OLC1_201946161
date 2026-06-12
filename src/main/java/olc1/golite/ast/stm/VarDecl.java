package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.symbols.GoliteType;
import olc1.golite.visitor.Visitor;

public class VarDecl implements ASTNode {

    private final String name;
    private final GoliteType declaredType; // null => inferir (:=)
    private final ASTNode value;           // null => sin valor inicial (default)
    private final int line;
    private final int column;

    public VarDecl(String name, GoliteType declaredType, ASTNode value, int line, int column) {
        this.name = name;
        this.declaredType = declaredType;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final String name;
        public final GoliteType declaredType;
        public final ASTNode value;
        public final int line;
        public final int column;

        public Context(VarDecl node) {
            this.name = node.name;
            this.declaredType = node.declaredType;
            this.value = node.value;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
