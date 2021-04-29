
package health.ere.ps.exception.idp;

public class IdpJoseException extends RuntimeException {

    private static final long serialVersionUID = -838371828368858466L;
    private final boolean containsSensitiveInformation;

    public IdpJoseException(final Exception e) {
        super(e);
        containsSensitiveInformation = true;
    }

    public IdpJoseException(final String message, final Exception e) {
        super(message, e);
        containsSensitiveInformation = true;
    }

    public IdpJoseException(final String s) {
        super(s);
        containsSensitiveInformation = true;
    }

    public IdpJoseException(final String message, final boolean containsSensitiveInformation, final Exception e) {
        super(message, e);
        this.containsSensitiveInformation = containsSensitiveInformation;
    }

    public String getMessageForUntrustedClients() {
        if (containsSensitiveInformation) {
            return "Error during JOSE-operations";
        } else {
            return getMessage();
        }
    }
}
