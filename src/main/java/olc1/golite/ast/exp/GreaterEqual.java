package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Mayor o igual que (>=)
public class GreaterEqual implements ASTNode {

    private final ASTNode left;
    private final ASTNode right;

    public GreaterEqual(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }

    public static class Context {

        public final ASTNode left;
        public final ASTNode right;

        public Context(GreaterEqual node) {
            this.left = node.left;
            this.right = node.right;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
