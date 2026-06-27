package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// append(slice, valor): devuelve un slice nuevo con el valor agregado
public class Append implements ASTNode {

    private final ASTNode slice;
    private final ASTNode value;
    private final int line;
    private final int column;

    public Append(ASTNode slice, ASTNode value, int line, int column) {
        this.slice = slice;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final ASTNode slice;
        public final ASTNode value;
        public final int line;
        public final int column;

        public Context(Append node) {
            this.slice = node.slice;
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
