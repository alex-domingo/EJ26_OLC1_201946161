package olc1.golite.visitor.interpreter.control;

import olc1.golite.visitor.interpreter.value.ValueWrapper;

// Señal interna para el return: lleva el valor de retorno (null si no hay valor).
public class ReturnSignal extends RuntimeException {

    private final ValueWrapper value;
    private final int line;
    private final int column;

    public ReturnSignal(ValueWrapper value, int line, int column) {
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public ValueWrapper getValue() {
        return value;
    }

    public boolean hasValue() {
        return value != null;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
