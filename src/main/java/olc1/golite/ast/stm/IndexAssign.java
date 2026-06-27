package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Modificacion de un elemento de slice: target[index] = value
// target es una expresion que debe evaluar a un slice (permite m[i][j] = x)
public class IndexAssign implements ASTNode {

    private final ASTNode target;
    private final ASTNode index;
    private final ASTNode value;
    private final int line;
    private final int column;

    public IndexAssign(ASTNode target, ASTNode index, ASTNode value, int line, int column) {
        this.target = target;
        this.index = index;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final ASTNode target;
        public final ASTNode index;
        public final ASTNode value;
        public final int line;
        public final int column;

        public Context(IndexAssign node) {
            this.target = node.target;
            this.index = node.index;
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
