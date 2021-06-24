package health.ere.ps.event.erixa;

import health.ere.ps.model.erixa.ErixaUploadMessagePayload;

public class ErixaUploadEvent {

    private final ErixaUploadMessagePayload load;

    public ErixaUploadEvent(ErixaUploadMessagePayload load) {
        this.load = load;
    }

    public ErixaUploadMessagePayload getLoad() {
        return load;
    }
}
