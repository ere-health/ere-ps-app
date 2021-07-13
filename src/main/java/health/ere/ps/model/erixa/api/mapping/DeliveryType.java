package health.ere.ps.model.erixa.api.mapping;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliveryType {
    SELF_COLLECT("SelfCollect"), HOME_DELIVERY("HomeDelivery"), SHIPPING("Shipping ");

    @JsonValue
    public final String value;

    DeliveryType(String value) {
        this.value = value;
    }
}
