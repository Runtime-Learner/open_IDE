package General_Purpose_IDE.Exceptions;

public class ReservedNameException extends Exception {
    public ReservedNameException(String type) {
        super(type);
    }
}
