package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// strings.Join(slice, separador): une un []string en una sola cadena
public class StringsJoin implements ASTNode {

    private final ASTNode slice;
    private final ASTNode separator;
    private final int line;
    private final int column;

    public StringsJoin(ASTNode slice, ASTNode separator, int line, int column) {
        this.slice = slice;
        this.separator = separator;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final ASTNode slice;
        public final ASTNode separator;
        public final int line;
        public final int column;

        public Context(StringsJoin node) {
            this.slice = node.slice;
            this.separator = node.separator;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
