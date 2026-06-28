package olc1.golite.ast.exp;

import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// Instanciacion de struct: Nombre{ campo: valor, ... }
public class StructLiteral implements ASTNode {

    // una asignacion campo: valor dentro de la instanciacion
    public static class FieldInit {

        public final String name;
        public final ASTNode value;
        public final int line;
        public final int column;

        public FieldInit(String name, ASTNode value, int line, int column) {
            this.name = name;
            this.value = value;
            this.line = line;
            this.column = column;
        }
    }

    private final String structName;
    private final List<FieldInit> inits;
    private final int line;
    private final int column;

    public StructLiteral(String structName, List<FieldInit> inits, int line, int column) {
        this.structName = structName;
        this.inits = inits;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final String structName;
        public final List<FieldInit> inits;
        public final int line;
        public final int column;

        public Context(StructLiteral node) {
            this.structName = node.structName;
            this.inits = node.inits;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
