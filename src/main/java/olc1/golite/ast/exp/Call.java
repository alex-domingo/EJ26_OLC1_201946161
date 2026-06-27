package olc1.golite.ast.exp;

import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Llamada a una funcion: nombre y lista de argumentos.
public class Call implements ASTNode {

    private final String name;
    private final List<ASTNode> args;
    private final int line;
    private final int column;

    public Call(String name, List<ASTNode> args, int line, int column) {
        this.name = name;
        this.args = args;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final String name;
        public final List<ASTNode> args;
        public final int line;
        public final int column;

        public Context(Call node) {
            this.name = node.name;
            this.args = node.args;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
