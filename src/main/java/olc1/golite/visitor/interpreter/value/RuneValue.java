package olc1.golite.visitor.interpreter.value;

public record RuneValue(int value, int line, int column) implements ValueWrapper {

    @Override
    public String getTypeName() {
        return "rune";
    }

    @Override
    public String toString() {
        return String.valueOf((char) value);
    }
}
