package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Modificacion de atributo: target.field = value
public class FieldAssign implements ASTNode {

    private final ASTNode target;
    private final String field;
    private final ASTNode value;
    private final int line;
    private final int column;

    public FieldAssign(ASTNode target, String field, ASTNode value, int line, int column) {
        this.target = target;
        this.field = field;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final ASTNode target;
        public final String field;
        public final ASTNode value;
        public final int line;
        public final int column;

        public Context(FieldAssign node) {
            this.target = node.target;
            this.field = node.field;
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
