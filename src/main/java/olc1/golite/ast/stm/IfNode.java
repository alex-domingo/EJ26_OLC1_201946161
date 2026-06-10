package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class IfNode implements ASTNode {

    private final ASTNode condition;
    private final ASTNode body;

    public IfNode(ASTNode condition, ASTNode body) {
        this.condition = condition;
        this.body = body;
    }

    public static class Context {

        public final ASTNode condition;
        public final ASTNode body;

        public Context(IfNode node) {
            this.condition = node.condition;
            this.body = node.body;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
