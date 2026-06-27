package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Modificacion de un elemento de slice: name[index] = value
public class IndexAssign implements ASTNode {

    private final String name;
    private final ASTNode index;
    private final ASTNode value;
    private final int line;
    private final int column;

    public IndexAssign(String name, ASTNode index, ASTNode value, int line, int column) {
        this.name = name;
        this.index = index;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final String name;
        public final ASTNode index;
        public final ASTNode value;
        public final int line;
        public final int column;

        public Context(IndexAssign node) {
            this.name = node.name;
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
