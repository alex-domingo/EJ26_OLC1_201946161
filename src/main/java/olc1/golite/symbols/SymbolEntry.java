package olc1.golite.symbols;

import olc1.golite.visitor.interpreter.value.ValueWrapper;

public class SymbolEntry {

    private final String name;
    private final GoliteType type;
    private ValueWrapper value;
    private final int line;
    private final int column;

    public SymbolEntry(String name, GoliteType type, ValueWrapper value, int line, int column) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public GoliteType getType() {
        return type;
    }

    public ValueWrapper getValue() {
        return value;
    }

    public void setValue(ValueWrapper value) {
        this.value = value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
