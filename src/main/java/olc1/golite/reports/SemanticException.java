package olc1.golite.reports;

public class SemanticException extends RuntimeException {

    private final GoliteError error;

    public SemanticException(GoliteError error) {
        super(error.toString());
        this.error = error;
    }

    public GoliteError getError() {
        return error;
    }
}
