package health.ere.ps.exception.gematik;

public class ERezeptWorkflowException extends Exception {

    public ERezeptWorkflowException(String message, Throwable e) {
        super(message, e);
    }

    public ERezeptWorkflowException(String message) {
        super(message);
    }
}
