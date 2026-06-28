package olc1.golite.symbols;

// Representa un tipo: un base primitivo + dimensiones de slice, O un struct con nombre.
// Primitivo: base != null, structName == null, dims = 0
// Slice:     dims >= 1 (sobre un base primitivo o un struct)
// Struct:    structName != null, base == null
public class GType {

    private final GoliteType base;     // null si es struct
    private final String structName;   // null si es primitivo
    private final int dimensions;

    public GType(GoliteType base, int dimensions) {
        this.base = base;
        this.structName = null;
        this.dimensions = dimensions;
    }

    public GType(String structName, int dimensions) {
        this.base = null;
        this.structName = structName;
        this.dimensions = dimensions;
    }

    private GType(GoliteType base, String structName, int dimensions) {
        this.base = base;
        this.structName = structName;
        this.dimensions = dimensions;
    }

    public static final GType INT = new GType(GoliteType.INT, 0);
    public static final GType FLOAT64 = new GType(GoliteType.FLOAT64, 0);
    public static final GType STRING = new GType(GoliteType.STRING, 0);
    public static final GType BOOL = new GType(GoliteType.BOOL, 0);
    public static final GType RUNE = new GType(GoliteType.RUNE, 0);
    public static final GType VOID = new GType(GoliteType.VOID, 0);

    // crea un tipo struct (dimension 0)
    public static GType struct(String name) {
        return new GType(name, 0);
    }

    public GoliteType getBase() {
        return base;
    }

    public String getStructName() {
        return structName;
    }

    public int getDimensions() {
        return dimensions;
    }

    public boolean isSlice() {
        return dimensions > 0;
    }

    public boolean isPrimitive() {
        return dimensions == 0 && base != null;
    }

    public boolean isStruct() {
        return dimensions == 0 && structName != null;
    }

    public GType sliceOf() {
        return new GType(base, structName, dimensions + 1);
    }

    public GType elementType() {
        return new GType(base, structName, dimensions - 1);
    }

    public String getLabel() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dimensions; i++) {
            sb.append("[]");
        }
        sb.append(structName != null ? structName : base.getLabel());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GType other)) {
            return false;
        }
        if (dimensions != other.dimensions) {
            return false;
        }
        if (structName != null) {
            return structName.equals(other.structName);
        }
        return base == other.base && other.structName == null;
    }

    @Override
    public int hashCode() {
        int h = (structName != null) ? structName.hashCode() : (base != null ? base.hashCode() : 0);
        return h * 31 + dimensions;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
