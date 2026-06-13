package olc1.golite.visitor.interpreter.control;

// Señal interna para el break, no es un error, la usamos para cortar el ciclo
public class BreakSignal extends RuntimeException {

    private final int line;
    private final int column;

    public BreakSignal(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
