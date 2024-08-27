package health.ere.ps.service.cetp.tracker;

import java.time.Instant;

public record BillingInfo(
    Instant timestamp,
    String ctId,
    String mandantId,
    String workplaceId,
    String clientSystemId
) {
    @Override
    public String toString() {
        return String.format("%s;%s;%s;%s;%s", timestamp, ctId, mandantId, workplaceId, clientSystemId);
    }
}
