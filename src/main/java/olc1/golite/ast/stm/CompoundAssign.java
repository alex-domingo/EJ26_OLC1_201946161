package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Asignacion compuesta += y -=  (op guarda '+' o '-')
public class CompoundAssign implements ASTNode {

    private final String name;
    private final char op;
    private final ASTNode value;
    private final int line;
    private final int column;

    public CompoundAssign(String name, char op, ASTNode value, int line, int column) {
        this.name = name;
        this.op = op;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final String name;
        public final char op;
        public final ASTNode value;
        public final int line;
        public final int column;

        public Context(CompoundAssign node) {
            this.name = node.name;
            this.op = node.op;
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
