package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Acceso a atributo: target.field (lectura)
public class FieldAccess implements ASTNode {

    private final ASTNode target;
    private final String field;
    private final int line;
    private final int column;

    public FieldAccess(ASTNode target, String field, int line, int column) {
        this.target = target;
        this.field = field;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final ASTNode target;
        public final String field;
        public final int line;
        public final int column;

        public Context(FieldAccess node) {
            this.target = node.target;
            this.field = node.field;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
