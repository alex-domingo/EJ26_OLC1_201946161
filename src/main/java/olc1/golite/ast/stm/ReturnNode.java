package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Sentencia return. expression en null => return sin valor.
public class ReturnNode implements ASTNode {

    private final ASTNode expression;
    private final int line;
    private final int column;

    public ReturnNode(ASTNode expression, int line, int column) {
        this.expression = expression;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final ASTNode expression;
        public final int line;
        public final int column;

        public Context(ReturnNode node) {
            this.expression = node.expression;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
