package health.ere.ps.model.muster16;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class Muster16PrescriptionFormProducer {
    @Produces
    @RequestScoped
    public Muster16PrescriptionForm muster16PrescriptionFormProducer() {
        return new Muster16PrescriptionForm();
    }
}
