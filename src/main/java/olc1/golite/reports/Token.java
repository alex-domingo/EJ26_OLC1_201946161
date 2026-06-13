package olc1.golite.reports;

// Representa un token reconocido por el lexer, para el reporte de tokens
public class Token {

    private final String tipo;
    private final String lexema;
    private final int linea;
    private final int columna;

    public Token(String tipo, String lexema, int linea, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
    }

    public String getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }
}
