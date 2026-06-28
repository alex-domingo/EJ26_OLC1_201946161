package olc1.golite.visitor.interpreter.value;

import java.util.Map;

import olc1.golite.symbols.GType;

// Instancia de un struct. Guarda un Map mutable de atributos -> valor.
// Como el Map es compartido, modificar un atributo muta la instancia real
// (semantica por referencia).
public final class StructValue implements ValueWrapper {

    private final String structName;
    private final Map<String, ValueWrapper> fields; // mutable y compartido
    private final int line;
    private final int column;

    public StructValue(String structName, Map<String, ValueWrapper> fields, int line, int column) {
        this.structName = structName;
        this.fields = fields;
        this.line = line;
        this.column = column;
    }

    public String getStructName() {
        return structName;
    }

    public Map<String, ValueWrapper> getFields() {
        return fields;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public int column() {
        return column;
    }

    @Override
    public String getTypeName() {
        return structName;
    }

    @Override
    public String toString() {
        // formato del enunciado: Persona{Nombre: Alice, Edad: 25, EsEstudiante: true}
        StringBuilder sb = new StringBuilder(structName).append("{");
        boolean first = true;
        for (Map.Entry<String, ValueWrapper> e : fields.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append(": ").append(e.getValue().toString());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
