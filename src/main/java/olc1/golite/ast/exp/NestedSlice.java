package olc1.golite.ast.exp;

import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Fila anidada de un literal multidimensional: {e1, e2, ...} SIN tipo declarado.
// El tipo se resuelve contra el tipo del literal externo que la contiene.
public class NestedSlice implements ASTNode {

    private final List<ASTNode> elements;
    private final int line;
    private final int column;

    public NestedSlice(List<ASTNode> elements, int line, int column) {
        this.elements = elements;
        this.line = line;
        this.column = column;
    }

    public List<ASTNode> getElements() {
        return elements;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public static class Context {

        public final List<ASTNode> elements;
        public final int line;
        public final int column;

        public Context(NestedSlice node) {
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
