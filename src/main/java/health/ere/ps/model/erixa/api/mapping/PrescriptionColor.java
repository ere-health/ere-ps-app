package health.ere.ps.model.erixa.api.mapping;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PrescriptionColor {
    RED("Red"), BLUE("Blue"), GREEN("Green");

    @JsonValue
    final String value;

    PrescriptionColor(String value) {
        this.value = value;
    }
}
