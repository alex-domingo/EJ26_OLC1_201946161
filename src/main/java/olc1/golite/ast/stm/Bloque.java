package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Bloque de sentencias { }, genera su propio ambito al ejecutarse
public class Bloque implements ASTNode {

    private final Statments body; // puede ser null si el bloque esta vacio

    public Bloque(Statments body) {
        this.body = body;
    }

    public static class Context {

        public final Statments body;

        public Context(Bloque node) {
            this.body = node.body;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
