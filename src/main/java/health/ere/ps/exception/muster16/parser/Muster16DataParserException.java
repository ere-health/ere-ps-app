package health.ere.ps.exception.muster16.parser;

public class Muster16DataParserException extends Exception {
    public Muster16DataParserException(String message, Throwable e) {
        super(message, e);
    }

    public Muster16DataParserException(String message) {
        super(message);
    }
}
