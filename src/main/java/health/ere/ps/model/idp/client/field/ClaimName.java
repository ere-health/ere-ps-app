package health.ere.ps.model.idp.client.field;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ClaimName {
    GIVEN_NAME("given_name"),
    FAMILY_NAME("family_name"),
    ORGANIZATION_NAME("organizationName"),
    PROFESSION_OID("professionOID"),
    ID_NUMBER("idNummer"),
    ISSUED_AT("iat"),
    AUTH_TIME("auth_time"),
    ISSUER("iss"),
    EXPIRES_AT("exp"),
    ALGORITHM("alg"),
    RESPONSE_TYPE("response_type"),
    SCOPE("scope"),
    CLIENT_ID("client_id"),
    STATE("state"),
    REDIRECT_URI("redirect_uri"),
    TYPE("typ"),
    CONTENT_TYPE("cty"),
    JWT_ID("jti"),
    KEY_ID("kid"),
    CLIENT_SIGNATURE("csig"),
    NESTED_JWT("njwt"),
    CODE_CHALLENGE("code_challenge"), // (Hashwert des "code_verifier") [RFC7636 # section-4.2]
    CODE_CHALLENGE_METHOD("code_challenge_method"), // HASH-Algorithmus (S256) [RFC7636 # section-4.3]
    CODE_VERIFIER("code_verifier"),
    CONFIRMATION("cnf"), // gemSpec_IDP_Dienst
    CLAIMS("claims"), // gemSpec_IDP_Dienst
    AUTHENTICATION_CLASS_REFERENCE("acr"), // https://openid.net/specs/openid-connect-core-1_0.html#IDToken
    AUTHORIZED_PARTY("azp"),
    SUBJECT("sub"),
    X509_CERTIFICATE_CHAIN("x5c"),
    SERVER_NONCE("snc"),
    AUDIENCE("aud"),
    JWKS_URI("jwks_uri"),
    ACR_VALUES_SUPPORTED("acr_values_supported"),
    TOKEN_TYPE("token_type"),
    TOKEN_KEY("token_key"),
    NONCE("nonce"),
    ACCESS_TOKEN_HASH("at_hash"),
    AUTHENTICATION_DATA("authentication_data"),
    AUTHENTICATION_DATA_VERSION("authentication_data_version"),
    AUTHENTICATION_METHODS_REFERENCE("amr"),
    AUTHENTICATION_CERTIFICATE("auth_cert"),
    AUTH_CERT_SUBJECT_PUBLIC_KEY_INFO("auth_cert_subject_public_key_info"),
    SE_SUBJECT_PUBLIC_KEY_INFO("se_subject_public_key_info"),
    KEY_IDENTIFIER("key_identifier"),
    CHALLENGE_TOKEN("challenge_token"),
    DEVICE_INFORMATION("device_information"),
    DEVICE_NAME("name"),
    DEVICE_TYPE("device_type"),
    DEVICE_MANUFACTURER("manufacturer"),
    DEVICE_PRODUCT("product"),
    DEVICE_MODEL("model"),
    DEVICE_OS("os"),
    DEVICE_OS_VERSION("version"),
    SIGNED_PAIRING_DATA("signed_pairing_data"),
    PAIRING_DATA_VERSION("pairing_data_version"),
    PAIRING_DATA("pairing_data"),
    AUTHORITY_INFO_ACCESS("authority_info_access"),
    ENCRYPTION_ALGORITHM("enc"),
    CERTIFICATE_SERIALNUMBER("serialnumber"),
    CERTIFICATE_ISSUER("issuer"),
    CERTIFICATE_NOT_AFTER("not_after"),
    SIGNATURE_ALGORITHM_IDENTIFIER("signature_algorithm_identifier");

    @JsonValue
    private String joseName;

    ClaimName(String joseName) {
        this.joseName = joseName;
    }

    public String getJoseName() {
        return joseName;
    }
}
