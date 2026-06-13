package olc1.golite.visitor.interpreter.control;

// Señal interna para el continue, no es un error, la usamos para cortar la iteración actual
public class ContinueSignal extends RuntimeException {

    private final int line;
    private final int column;

    public ContinueSignal(int line, int column) {
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
