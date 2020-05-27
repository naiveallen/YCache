package exception;

public class NullCommandException extends Exception {

    public NullCommandException() {
        super();
    }

    public NullCommandException(String s) {
        super(s);
    }
}
