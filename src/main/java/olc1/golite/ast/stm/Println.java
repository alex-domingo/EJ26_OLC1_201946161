package olc1.golite.ast.stm;

import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// fmt.Println: imprime los argumentos separados por espacio y termina con salto de linea
public class Println implements ASTNode {

    private final List<ASTNode> args;
    private final int line;
    private final int column;

    public Println(List<ASTNode> args, int line, int column) {
        this.args = args;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final List<ASTNode> args;
        public final int line;
        public final int column;

        public Context(Println node) {
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
