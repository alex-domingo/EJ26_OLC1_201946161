package olc1.golite.visitor.interpreter.value;

import java.util.List;

import olc1.golite.symbols.GType;

// Valor de un slice: lista de elementos + su tipo (ej. []int).
public final class SliceValue implements ValueWrapper {

    private final List<ValueWrapper> elements;
    private final GType type;
    private final int line;
    private final int column;

    public SliceValue(List<ValueWrapper> elements, GType type, int line, int column) {
        this.elements = elements;
        this.type = type;
        this.line = line;
        this.column = column;
    }

    public List<ValueWrapper> getElements() {
        return elements;
    }

    public GType getType() {
        return type;
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
        return type.getLabel();
    }

    @Override
    public String toString() {
        // formato estilo Go: [a b c]
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(elements.get(i).toString());
        }
        sb.append("]");
        return sb.toString();
    }
}
