package com.erehealth.ps.service.fhir.bundle;

import com.erehealth.ps.model.muster16.Muster16PrescriptionForm;

import org.hl7.fhir.r4.model.Bundle;

public class PrescriptionBundleBuilder {
    private Muster16PrescriptionForm muster16PrescriptionForm;

    public PrescriptionBundleBuilder(Muster16PrescriptionForm muster16PrescriptionForm) {
        this.muster16PrescriptionForm = muster16PrescriptionForm;
    }

    public Bundle createBundle() {
        Bundle bundle = new Bundle();

        //TODO: Build the bundle from muster 16 form object.

        return bundle;
    }
}
