package olc1.golite.ast.exp;

import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.symbols.GType;
import olc1.golite.visitor.Visitor;

// Literal de slice: []T{e1, e2, ...}. 'type' es el tipo del slice (ej. []int).
public class SliceLiteral implements ASTNode {

    private final GType type;
    private final List<ASTNode> elements;
    private final int line;
    private final int column;

    public SliceLiteral(GType type, List<ASTNode> elements, int line, int column) {
        this.type = type;
        this.elements = elements;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final GType type;
        public final List<ASTNode> elements;
        public final int line;
        public final int column;

        public Context(SliceLiteral node) {
            this.type = node.type;
            this.elements = node.elements;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
