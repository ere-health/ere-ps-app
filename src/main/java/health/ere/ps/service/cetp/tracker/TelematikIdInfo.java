package health.ere.ps.service.cetp.tracker;

public record TelematikIdInfo(
    String timestamp,
    String telematikId
) {
    @Override
    public String toString() {
        return String.format("%s,%s\n", timestamp, telematikId);
    }
}