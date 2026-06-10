package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

public class Imprimir implements ASTNode {

    private final ASTNode expression;

    public Imprimir(ASTNode expression) {
        this.expression = expression;
    }

    public static class Context {

        public final ASTNode expression;

        public Context(Imprimir node) {
            this.expression = node.expression;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }

}
