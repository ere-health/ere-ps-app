package health.ere.ps.service.fhir.bundle;

import org.hl7.fhir.r4.model.Bundle;

import java.util.List;

import health.ere.ps.model.muster16.MedicationString;

public interface IBundlesBuilder {
    List<Bundle> createBundles();
    Bundle createBundleForMedication(MedicationString medication);
}
