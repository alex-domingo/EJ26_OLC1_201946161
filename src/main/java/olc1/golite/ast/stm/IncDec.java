package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Incremento i++ (op guarda '+'; lo dejamos generico por si luego sumamos i--)
public class IncDec implements ASTNode {

    private final String name;
    private final char op;
    private final int line;
    private final int column;

    public IncDec(String name, char op, int line, int column) {
        this.name = name;
        this.op = op;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final String name;
        public final char op;
        public final int line;
        public final int column;

        public Context(IncDec node) {
            this.name = node.name;
            this.op = node.op;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
