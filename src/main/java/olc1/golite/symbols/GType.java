package olc1.golite.symbols;

// Representa un tipo: un base primitivo + cantidad de dimensiones de slice.
// dimensions = 0 -> primitivo ; >= 1 -> slice.
// int -> base=INT,dim=0 | []int -> base=INT,dim=1 | [][]int -> base=INT,dim=2
public class GType {

    private final GoliteType base;
    private final int dimensions;

    public GType(GoliteType base, int dimensions) {
        this.base = base;
        this.dimensions = dimensions;
    }

    // instancias listas para los tipos primitivos
    public static final GType INT = new GType(GoliteType.INT, 0);
    public static final GType FLOAT64 = new GType(GoliteType.FLOAT64, 0);
    public static final GType STRING = new GType(GoliteType.STRING, 0);
    public static final GType BOOL = new GType(GoliteType.BOOL, 0);
    public static final GType RUNE = new GType(GoliteType.RUNE, 0);
    public static final GType VOID = new GType(GoliteType.VOID, 0);

    public GoliteType getBase() {
        return base;
    }

    public int getDimensions() {
        return dimensions;
    }

    public boolean isSlice() {
        return dimensions > 0;
    }

    public boolean isPrimitive() {
        return dimensions == 0;
    }

    // []T a partir de T (una dimension mas)
    public GType sliceOf() {
        return new GType(base, dimensions + 1);
    }

    // T a partir de []T (una dimension menos)
    public GType elementType() {
        return new GType(base, dimensions - 1);
    }

    // etiqueta legible: "int", "[]int", "[][]string", etc.
    public String getLabel() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dimensions; i++) {
            sb.append("[]");
        }
        sb.append(base.getLabel());
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
        return base == other.base && dimensions == other.dimensions;
    }

    @Override
    public int hashCode() {
        return base.hashCode() * 31 + dimensions;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
