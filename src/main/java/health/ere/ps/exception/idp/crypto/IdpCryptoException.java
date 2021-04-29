package health.ere.ps.exception.idp.crypto;

public class IdpCryptoException extends RuntimeException {

    private static final long serialVersionUID = 6861433495462078391L;

    public IdpCryptoException(final Exception e) {
        super(e);
    }

    public IdpCryptoException(final String s) {
        super(s);
    }

    public IdpCryptoException(final String s, final Exception ex) {
        super(s, ex);
    }
}
