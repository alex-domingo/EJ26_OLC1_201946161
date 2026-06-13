package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Ciclo for, init y post pueden ser null (forma tipo while)
public class ForNode implements ASTNode {

    private final ASTNode init;       // null en la forma while
    private final ASTNode condition;
    private final ASTNode post;       // null en la forma while
    private final ASTNode body;       // siempre un Bloque

    public ForNode(ASTNode init, ASTNode condition, ASTNode post, ASTNode body) {
        this.init = init;
        this.condition = condition;
        this.post = post;
        this.body = body;
    }

    public static class Context {

        public final ASTNode init;
        public final ASTNode condition;
        public final ASTNode post;
        public final ASTNode body;

        public Context(ForNode node) {
            this.init = node.init;
            this.condition = node.condition;
            this.post = node.post;
            this.body = node.body;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
