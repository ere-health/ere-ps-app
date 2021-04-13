package com.erehealth.ps.service.fhir.bundle;

import com.erehealth.ps.model.muster16.Muster16PrescriptionForm;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

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

    public Patient createPatientResource() throws ParseException {
        Patient patient = new Patient();

        patient.setId(muster16PrescriptionForm.getPatientInsuranceId());
        patient.getMeta().addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient" +
                "|1.0.3");

        Identifier identifier = patient.addIdentifier();

        identifier.getType().addCoding().setSystem("http://fhir" +
                ".de/CodeSystem/identifier-type-de-basis");
        identifier.getType().addCoding().setCode("GKV");
        identifier.getSystemElement().setValue("http://fhir.de/NamingSystem/gkv/kvid-10");
        identifier.setValue("M310119800");

        patient.addName().setUse(
                HumanName.NameUse.OFFICIAL).setFamily(
                        muster16PrescriptionForm.getPatientLastName()).addGiven(
                                muster16PrescriptionForm.getPatientFirstName());

        patient.setBirthDate(
                DateFormat.getDateInstance(
                        DateFormat.LONG, Locale.GERMANY).parse(
                                muster16PrescriptionForm.getPatientDateOfBirth()));

        patient.addAddress().setCity(
                muster16PrescriptionForm.getPatientCity()).setPostalCode(
                        muster16PrescriptionForm.getPatientZipCode()).addLine(
                                muster16PrescriptionForm.getPatientStreetName() + " " +
                                muster16PrescriptionForm.getPatientStreetNumber());
        return patient;
    }

    public Practitioner createPractitionerResource() {
        Practitioner practitioner = new Practitioner();

        practitioner.setId(muster16PrescriptionForm.getDoctorId())
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3");

        Identifier identifier = practitioner.addIdentifier();

        identifier.getType().addCoding().setSystem(
                "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR");
        identifier.getSystemElement().setValue("456456534");
        identifier.setValue("M310119800");
        
        return practitioner;
    }

    public Organization createOrganizationResource() {
        Organization organization = new Organization();

        organization.setId(muster16PrescriptionForm.getClinicId()).getMeta().addProfile(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3");

        return organization;
    }

    public Coverage createCoverageResource() throws ParseException {
        Coverage coverage = new Coverage();

        coverage.setId(muster16PrescriptionForm.getInsuranceCompanyId())
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3");

        coverage.setStatus(Coverage.CoverageStatus.ACTIVE);
        coverage.getBeneficiary().setReference(
                "Patient/" + muster16PrescriptionForm.getPatientInsuranceId());
        coverage.getPeriod().setEnd(DateFormat.getDateInstance(DateFormat.LONG, Locale.GERMANY)
                .parse(muster16PrescriptionForm.getPrescriptionDate()));

        return coverage;
    }

    public Medication createMedicationResource() {
        Medication medication = new Medication();

        medication.setId("") // TODO: Get actual prescription ID.
            .getMeta()
            .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1");

        muster16PrescriptionForm.getPrescriptionList().stream().forEach(prescription -> {
            medication.getCode().addCoding()
                .setSystem("http://fhir.de/CodeSystem/ifa/pzn")
                .setCode("08585997");
            medication.getCode().setText(prescription);
        });

        return medication;
    }

    public MedicationRequest createMedicationRequest() {
        MedicationRequest medicationRequest = new MedicationRequest();

        return medicationRequest;
    }

    public Composition createComposition() {
        Composition composition = new Composition();

        return composition;
    }
}
