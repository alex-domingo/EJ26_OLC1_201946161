package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Acceso indexado a un slice: target[index]
public class Index implements ASTNode {

    private final ASTNode target;
    private final ASTNode index;
    private final int line;
    private final int column;

    public Index(ASTNode target, ASTNode index, int line, int column) {
        this.target = target;
        this.index = index;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final ASTNode target;
        public final ASTNode index;
        public final int line;
        public final int column;

        public Context(Index node) {
            this.target = node.target;
            this.index = node.index;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
