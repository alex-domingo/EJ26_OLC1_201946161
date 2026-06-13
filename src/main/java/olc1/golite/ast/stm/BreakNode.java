package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Sentencia break
public class BreakNode implements ASTNode {

    private final int line;
    private final int column;

    public BreakNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final int line;
        public final int column;

        public Context(BreakNode node) {
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
