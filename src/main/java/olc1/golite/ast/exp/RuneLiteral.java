package olc1.golite.ast.exp;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Literal rune, guardamos el lexema con comillas y lo convertimos en el interprete
public class RuneLiteral implements ASTNode {

    private final String lexeme;
    private final int line;
    private final int column;

    public RuneLiteral(String lexeme, int line, int column) {
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final String lexeme;
        public final int line;
        public final int column;

        public Context(RuneLiteral node) {
            this.lexeme = node.lexeme;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
