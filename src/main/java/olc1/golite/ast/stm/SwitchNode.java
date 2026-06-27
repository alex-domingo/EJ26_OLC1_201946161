package olc1.golite.ast.stm;

import java.util.List;

import olc1.golite.ast.ASTNode;
import olc1.golite.visitor.Visitor;

// switch / case / default. defaultBody puede ser null.
public class SwitchNode implements ASTNode {

    private final ASTNode expression;
    private final List<CaseClause> cases;
    private final ASTNode defaultBody; // Statments o null
    private final int line;
    private final int column;

    public SwitchNode(ASTNode expression, List<CaseClause> cases, ASTNode defaultBody, int line, int column) {
        this.expression = expression;
        this.cases = cases;
        this.defaultBody = defaultBody;
        this.line = line;
        this.column = column;
    }

    public static class Context {

        public final ASTNode expression;
        public final List<CaseClause> cases;
        public final ASTNode defaultBody;
        public final int line;
        public final int column;

        public Context(SwitchNode node) {
            this.expression = node.expression;
            this.cases = node.cases;
            this.defaultBody = node.defaultBody;
            this.line = node.line;
            this.column = node.column;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
