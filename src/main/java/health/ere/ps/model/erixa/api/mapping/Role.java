package health.ere.ps.model.erixa.api.mapping;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    PATIENT("Patient"), DOCTOR("Doctor"), DRUGSTORE("Drugstore");

    @JsonValue
    final String value;

    Role(String value) {
        this.value = value;
    }
}
