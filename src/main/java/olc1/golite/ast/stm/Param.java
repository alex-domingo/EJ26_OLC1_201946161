package olc1.golite.ast.stm;

import olc1.golite.symbols.GType;

public class Param {

    public final String name;
    public final GType type;
    public final int line;
    public final int column;

    public Param(String name, GType type, int line, int column) {
        this.name = name;
        this.type = type;
        this.line = line;
        this.column = column;
    }
}
