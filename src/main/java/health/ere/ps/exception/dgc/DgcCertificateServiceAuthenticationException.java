package health.ere.ps.exception.dgc;

/**
 * Exception that occurred when authenticating against the certificate service.
 */
public class DgcCertificateServiceAuthenticationException extends DgcException {
    public DgcCertificateServiceAuthenticationException(int code, String message) {
        super(code, message);
    }
}
