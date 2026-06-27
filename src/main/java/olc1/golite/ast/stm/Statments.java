package olc1.golite.ast.stm;

import java.util.ArrayList;
import java.util.List;

import olc1.golite.ast.ASTNode;

public class Statments implements ASTNode {

    private final List<ASTNode> statements;

    public Statments(ASTNode statement) {
        this.statements = new ArrayList<>();
        this.statements.add(statement);
    }

    public void add(ASTNode statement) {
        if (statement != null) {
            this.statements.add(statement);
        }
    }

    public class Context {

        public final List<ASTNode> statements;

        public Context(ASTNode node) {
            this.statements = ((Statments) node).statements;
        }
    }

    @Override
    public <T> T accept(olc1.golite.visitor.Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }

    public List<ASTNode> getStatements() {
        return statements;
    }
}
