package olc1.golite.symbols;

public enum GoliteType {
    INT("int"),
    FLOAT64("float64"),
    STRING("string"),
    BOOL("bool"),
    RUNE("rune"),
    VOID("void");

    private final String label;

    GoliteType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
