package health.ere.ps.exception.dgc;

/**
 * This exception will be used if any of the inputs has been invalid. This applies for both local invalid input as well
 * as certificate service invalid input.
 */
public class DgcInvalidParametersException extends DgcException {
    public DgcInvalidParametersException(int code, String message) {
        super(code, message);
    }
}
