package health.ere.ps.model.idp.client;

public class IdpClientRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -3280232274428362763L;

    public IdpClientRuntimeException(final Exception e) {
        super(e);
    }

    public IdpClientRuntimeException(final String s) {
        super(s);
    }

    public IdpClientRuntimeException(final String message, final Exception e) {
        super(message, e);
    }
}
