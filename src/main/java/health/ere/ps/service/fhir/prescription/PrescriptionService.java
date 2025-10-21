package health.ere.ps.service.fhir.prescription;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.hl7.fhir.r4.model.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jakarta.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static java.util.regex.Pattern.DOTALL;

@ApplicationScoped
public class PrescriptionService {

    private static final Pattern GET_BUNDLE = Pattern.compile("(<Bundle[^>]*>.*?</Bundle>)", DOTALL);

    private final AppPrescriptionProcessor appPrescriptionProcessor;
    private final MedicationPrescriptionProcessor medicationPrescriptionProcessor;

    @Inject
    public PrescriptionService(
        AppPrescriptionProcessor appPrescriptionProcessor,
        MedicationPrescriptionProcessor medicationPrescriptionProcessor
    ) {
        this.appPrescriptionProcessor = appPrescriptionProcessor;
        this.medicationPrescriptionProcessor = medicationPrescriptionProcessor;
    }

    public Bundle[] parseFromString(String xml) {
        List<Bundle> bundles = new ArrayList<>();
        Matcher m = GET_BUNDLE.matcher(xml);
        boolean found = false;
        while (m.find()) {
            found = true;
            String bundleXml = m.group(1);
            XmlPrescriptionProcessor prescriptionProcessor = xml.contains("KBV_PR_ERP_Bundle")
                ? medicationPrescriptionProcessor
                : appPrescriptionProcessor;

            Bundle bundle = prescriptionProcessor.createFixedBundleFromString(bundleXml);
            bundles.add(bundle);
        }
        if (!found) {
            throw new WebApplicationException("Could not extract inner text", NOT_ACCEPTABLE);
        }
        return bundles.toArray(new Bundle[]{});
    }
}
