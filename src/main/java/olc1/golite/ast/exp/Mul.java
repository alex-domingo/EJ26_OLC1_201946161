package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class Mul implements ASTNode {

    private final ASTNode left;
    private final ASTNode right;

    public Mul(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }

    public static class Context {

        public final ASTNode left;
        public final ASTNode right;

        public Context(Mul node) {
            this.left = node.left;
            this.right = node.right;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
