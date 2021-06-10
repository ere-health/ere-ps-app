package health.ere.ps.exception.dgc;

/**
 * Exception that has its origin in an internal server error in the certificate
 * creation service.
 * Unknown exceptions should also be mapped to this exception.
 */
public class DgcCertificateServiceException extends DgcException {
    public DgcCertificateServiceException(int code, String message) {
        super(code, message);
    }
}
