package health.ere.ps.model.idp.client.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import health.ere.ps.model.idp.client.error.IdpErrorType;

// Antwort Objekt, wenn eine OAuth2 / OICD Exception geworfen wurde.
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class IdpErrorResponse {
    // Error code laut OAuth2 / OICD spec.
    private IdpErrorType error;

    // Detaillierter gematik Fehlercode, 4-stellig
    @JsonProperty("gematik_code")
    private int code;

    // Zeitpunkt des Fehlers in Sekunden seit 01.01.1970 UTC.
    @JsonProperty("gematik_timestamp")
    private String timestamp;

    // eindeutige, generierte uuid für den Fehler
    @JsonProperty("gematik_uuid")
    private String errorUuid;

    // Fehlertext für den Endbenutzer.
    @JsonProperty("gematik_error_text")
    private String detailMessage;

    @JsonIgnore
    private int httpStatusCode;

    public IdpErrorResponse(IdpErrorType error, int code, String timestamp, String errorUuid,
                            String detailMessage, int httpStatusCode) {
        this.error = error;
        this.code = code;
        this.timestamp = timestamp;
        this.errorUuid = errorUuid;
        this.detailMessage = detailMessage;
        this.httpStatusCode = httpStatusCode;
    }

    public IdpErrorResponse() {
    }

    public IdpErrorType getError() {
        return error;
    }

    public void setError(IdpErrorType error) {
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorUuid() {
        return errorUuid;
    }

    public void setErrorUuid(String errorUuid) {
        this.errorUuid = errorUuid;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public void setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public static IdpErrorResponseBuilder builder() {
        return new IdpErrorResponseBuilder();
    }

    public static class IdpErrorResponseBuilder {
        private IdpErrorResponse idpErrorResponse;

        public IdpErrorResponseBuilder() {
            idpErrorResponse = new IdpErrorResponse();
        }

        public IdpErrorResponseBuilder error(IdpErrorType error) {
            idpErrorResponse.setError(error);

            return this;
        }

        public IdpErrorResponseBuilder code(int code) {
            idpErrorResponse.setCode(code);

            return this;
        }

        public IdpErrorResponseBuilder timestamp(String timestamp) {
            idpErrorResponse.setTimestamp(timestamp);

            return this;
        }

        public IdpErrorResponseBuilder errorUuid(String errorUuid) {
            idpErrorResponse.setErrorUuid(errorUuid);

            return this;
        }

        public IdpErrorResponseBuilder detailMessage(String detailMessage) {
            idpErrorResponse.setDetailMessage(detailMessage);

            return this;
        }

        public IdpErrorResponseBuilder httpStatusCode(int httpStatusCode) {
            idpErrorResponse.setHttpStatusCode(httpStatusCode);

            return this;
        }

        public IdpErrorResponse build() {
            return idpErrorResponse;
        }
    }
}
