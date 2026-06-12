package olc1.golite.visitor.interpreter.value;

public sealed interface ValueWrapper
        permits IntValue, DecimalValue, VoidValue, BoolValue, StringValue, RuneValue {

    int line();

    int column();

    String getTypeName();
}
