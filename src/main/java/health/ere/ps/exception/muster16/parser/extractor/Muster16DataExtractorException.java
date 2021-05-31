package health.ere.ps.exception.muster16.parser.extractor;

public class Muster16DataExtractorException extends Exception {
    public Muster16DataExtractorException(String message, Throwable e) {
        super(message, e);
    }

    public Muster16DataExtractorException(String message) {
        super(message);
    }
}
