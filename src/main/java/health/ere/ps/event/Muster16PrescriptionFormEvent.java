package health.ere.ps.event;

import health.ere.ps.model.muster16.Muster16PrescriptionForm;

public final class Muster16PrescriptionFormEvent {

    private final Muster16PrescriptionForm muster16PrescriptionForm;

    public Muster16PrescriptionFormEvent(Muster16PrescriptionForm muster16PrescriptionForm) {
        this.muster16PrescriptionForm = muster16PrescriptionForm;
    }

    public Muster16PrescriptionForm getMuster16PrescriptionForm() {
        return muster16PrescriptionForm;
    }
}
