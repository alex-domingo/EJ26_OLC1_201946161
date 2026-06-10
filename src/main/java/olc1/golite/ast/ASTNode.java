package olc1.golite.ast;

import olc1.golite.visitor.Visitor;

public interface ASTNode {

    <T> T accept(Visitor<T> visitor);
}
