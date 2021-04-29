package health.ere.ps.model.idp.client;

public final class IdpConstants {

    private IdpConstants(){}

    public static final String DISCOVERY_DOCUMENT_ENDPOINT = "/discoveryDocument";
    public static final String BASIC_AUTHORIZATION_ENDPOINT = "/sign_response";
    public static final String ALTERNATIVE_AUTHORIZATION_ENDPOINT = "/alt_response";
    public static final String SSO_ENDPOINT = "/sso_response";
    public static final String TOKEN_ENDPOINT = "/token";
    public static final String PAIRING_ENDPOINT = "/pairings";
    public static final String DEVICE_VALIDATION_ENDPOINT = "/device_validation";
    public static final String AUDIENCE = "https://erp.telematik.de/login";
    public static final String DEFAULT_SERVER_URL = "https://idp.zentral.idp.splitdns.ti-dienste.de";
    public static final String EIDAS_LOA_HIGH = "gematik-ehealth-loa-high";
    public static final int JTI_LENGTH = 16;
}
