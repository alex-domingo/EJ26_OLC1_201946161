package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// if / else if / else
// elseBranch puede ser null, otro IfNode (else if) o un Bloque (else)
public class IfNode implements ASTNode {

    private final ASTNode condition;
    private final ASTNode body;        // Bloque del if
    private final ASTNode elseBranch;  // null, IfNode o Bloque

    public IfNode(ASTNode condition, ASTNode body, ASTNode elseBranch) {
        this.condition = condition;
        this.body = body;
        this.elseBranch = elseBranch;
    }

    public static class Context {

        public final ASTNode condition;
        public final ASTNode body;
        public final ASTNode elseBranch;

        public Context(IfNode node) {
            this.condition = node.condition;
            this.body = node.body;
            this.elseBranch = node.elseBranch;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
