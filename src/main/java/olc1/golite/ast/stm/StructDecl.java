package olc1.golite.ast.stm;

import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.symbols.GType;
import olc1.golite.visitor.Visitor;

// Declaracion de struct: struct Nombre { tipo campo; ... }
public class StructDecl implements ASTNode {

    // un campo declarado en el struct
    public static class FieldDecl {

        public final String name;
        public final GType type;
        public final int line;
        public final int column;

        public FieldDecl(String name, GType type, int line, int column) {
            this.name = name;
            this.type = type;
            this.line = line;
            this.column = column;
        }
    }

    private final String name;
    private final List<FieldDecl> fields;
    private final int line;
    private final int column;

    public StructDecl(String name, List<FieldDecl> fields, int line, int column) {
        this.name = name;
        this.fields = fields;
        this.line = line;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public List<FieldDecl> getFields() {
        return fields;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public static class Context {

        public final int line;
        public final int column;

        public Context(StructDecl node) {
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
