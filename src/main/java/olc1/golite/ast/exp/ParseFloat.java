package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// strconv.ParseFloat: convierte una cadena en un float64
public class ParseFloat implements ASTNode {

    private final ASTNode expression;
    private final int line;
    private final int column;

    public ParseFloat(ASTNode expression, int line, int column) {
        this.expression = expression;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final ASTNode expression;
        public final int line;
        public final int column;

        public Context(ParseFloat node) {
            this.expression = node.expression;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
