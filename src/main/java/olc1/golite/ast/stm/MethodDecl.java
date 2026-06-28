package olc1.golite.ast.stm;

import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.symbols.GType;
import olc1.golite.visitor.Visitor;

// Metodo con receptor: func (recv Struct) nombre(params) tipo { }
public class MethodDecl implements ASTNode {

    private final String receiverName;    // ej. "p"
    private final String receiverStruct;  // ej. "Persona"
    private final String name;
    private final List<Param> params;
    private final GType returnType;       // null => void
    private final ASTNode body;
    private final int line;
    private final int column;

    public MethodDecl(String receiverName, String receiverStruct, String name,
            List<Param> params, GType returnType, ASTNode body, int line, int column) {
        this.receiverName = receiverName;
        this.receiverStruct = receiverStruct;
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.body = body;
        this.line = line;
        this.column = column;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverStruct() {
        return receiverStruct;
    }

    public String getName() {
        return name;
    }

    public List<Param> getParams() {
        return params;
    }

    public GType getReturnType() {
        return returnType;
    }

    public ASTNode getBody() {
        return body;
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

        public Context(MethodDecl node) {
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
