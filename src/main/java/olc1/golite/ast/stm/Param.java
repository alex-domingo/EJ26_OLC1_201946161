package olc1.golite.ast.stm;

import olc1.golite.symbols.GoliteType;

// Parametro de una funcion: nombre y tipo
public class Param {

    public final String name;
    public final GoliteType type;
    public final int line;
    public final int column;

    public Param(String name, GoliteType type, int line, int column) {
        this.name = name;
        this.type = type;
        this.line = line;
        this.column = column;
    }
}
