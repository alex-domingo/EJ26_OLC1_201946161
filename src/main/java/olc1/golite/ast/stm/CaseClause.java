package olc1.golite.ast.stm;

import olc1.golite.ast.ASTNode;

// Una clausula case del switch: el valor a comparar y su cuerpo de sentencias
public class CaseClause {

    public final ASTNode value;
    public final ASTNode body; // Statments o null si el case esta vacio

    public CaseClause(ASTNode value, ASTNode body) {
        this.value = value;
        this.body = body;
    }
}
