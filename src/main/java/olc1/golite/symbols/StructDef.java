package olc1.golite.symbols;

import java.util.List;

// Definicion de un struct: nombre y lista ordenada de atributos (nombre + tipo).
public class StructDef {

    // un atributo: nombre y tipo
    public static class Field {

        public final String name;
        public final GType type;
        public final int line;
        public final int column;

        public Field(String name, GType type, int line, int column) {
            this.name = name;
            this.type = type;
            this.line = line;
            this.column = column;
        }
    }

    private final String name;
    private final List<Field> fields;
    private final int line;
    private final int column;

    public StructDef(String name, List<Field> fields, int line, int column) {
        this.name = name;
        this.fields = fields;
        this.line = line;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    // busca un atributo por nombre; null si no existe
    public Field getField(String fieldName) {
        for (Field f : fields) {
            if (f.name.equals(fieldName)) {
                return f;
            }
        }
        return null;
    }
}
