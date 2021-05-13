package health.ere.ps.model.idp.client.error;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public enum IdpErrorType {
    INTERACTION_REQUIRED("interaction_required"),
    LOGIN_REQUIRED("login_required"),
    ACCOUNT_SELECTION_REQUIRED("account_selection_required"),
    CONSENT_REQUIRED("consent_required"),
    INVALID_REQUEST_URI("invalid_request_uri"),
    INVALID_REQUEST_OBJECT("invalid_request_object"),
    REQUEST_NOT_SUPPORTED("request_not_supported"),
    REQUEST_URI_NOT_SUPPORTED("request_uri_not_supported"),
    REGISTRATION_NOT_SUPPORTED("registration_not_supported"),
    INVALID_REQUEST("invalid_request"),
    UNAUTHORIZED_CLIENT("unauthorized_client"),
    ACCESS_DENIED("access_denied"),
    UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type"),
    INVALID_SCOPE("invalid_scope"),
    SERVER_ERROR("server_error"),
    TEMPORARILY_UNAVAILABLE("temporarily_unavailable"),
    INVALID_CLIENT("invalid_client"),
    INVALID_GRANT("invalid_grant"),
    UNSUPPORTED_GRANT_TYPE("unsupported_grant_type");

    private String serializationValue;

    IdpErrorType(String serializationValue) {
        this.serializationValue = serializationValue;
    }

    @JsonValue
    public String getSerializationValue() {
        return serializationValue;
    }
}
