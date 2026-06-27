package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// for indice, valor := range slice { ... }
public class ForRange implements ASTNode {

    private final String indexName;
    private final String valueName;
    private final ASTNode slice;
    private final ASTNode body; // Bloque
    private final int line;
    private final int column;

    public ForRange(String indexName, String valueName, ASTNode slice, ASTNode body, int line, int column) {
        this.indexName = indexName;
        this.valueName = valueName;
        this.slice = slice;
        this.body = body;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final String indexName;
        public final String valueName;
        public final ASTNode slice;
        public final ASTNode body;
        public final int line;
        public final int column;

        public Context(ForRange node) {
            this.indexName = node.indexName;
            this.valueName = node.valueName;
            this.slice = node.slice;
            this.body = node.body;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
