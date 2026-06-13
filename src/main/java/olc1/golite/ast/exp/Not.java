package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Negacion logica (!)
public class Not implements ASTNode {

    private final ASTNode expression;

    public Not(ASTNode expression) {
        this.expression = expression;
    }

    public static class Context {

        public final ASTNode expression;

        public Context(Not node) {
            this.expression = node.expression;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
