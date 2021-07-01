package health.ere.ps.exception.bundle;

public class EreParseException extends Exception {

    public EreParseException(String msg) {
        super(msg);
    }

    public EreParseException(String msg, Throwable e) {
        super(msg, e);
    }
}
