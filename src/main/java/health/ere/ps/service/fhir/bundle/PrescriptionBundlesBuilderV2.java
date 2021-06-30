package health.ere.ps.service.fhir.bundle;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.MedicationRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;

public class PrescriptionBundlesBuilderV2 extends PrescriptionBundlesBuilder {
    protected static final String $PRESCRIPTION_ID = "$PRESCRIPTION_ID";
    protected static final String $BUNDLE_ID = "$BUNDLE_ID";
    protected static final String $LAST_UPDATED = "$LAST_UPDATED";
    protected static final String $TIMESTAMP = "$TIMESTAMP";
    protected static final String $COMPOSITION_ID = "$COMPOSITION_ID";
    protected static final String $COMPOSITION_DATE = "$COMPOSITION_DATE";
    protected static final String $DEVICE_ID = "$DEVICE_ID";
    protected static final String $MEDICATION_REQUEST_ID = "$MEDICATION_REQUEST_ID";
    protected static final String $STATUS_CO_PAYMENT = "$STATUS_CO_PAYMENT";
    protected static final String $AUTHORED_ON = "$AUTHORED_ON";
    protected static final String $MEDICATION_ID = "$MEDICATION_ID";
    protected static final String $DOSAGE_QUANTITY = "$DOSAGE_QUANTITY";
    protected static final String $PZN = "$PZN";
    protected static final String $MEDICATION_NAME = "$MEDICATION_NAME";
    protected static final String $MEDICATION_FORM = "$MEDICATION_FORM";
    protected static final String $MEDICATION_SIZE = "$MEDICATION_SIZE";
    protected static final String $DOSAGE_TEXT = "$DOSAGE_TEXT";
    protected static final String $KVID_10 = "$KVID_10";
    protected static final String $PATIENT_ID = "$PATIENT_ID";
    protected static final String $PATIENT_NAME_PREFIX = "$PATIENT_NAME_PREFIX";
    protected static final String $PATIENT_NAME_FIRST = "$PATIENT_NAME_FIRST";
    protected static final String $PATIENT_NAME_FAMILY = "$PATIENT_NAME_FAMILY";
    protected static final String $PATIENT_BIRTH_DATE = "$PATIENT_BIRTH_DATE";
    protected static final String $PATIENT_ADDRESS_LINE = "$PATIENT_ADDRESS_LINE";
    protected static final String $PATIENT_ADDRESS_STREET_NAME = "$PATIENT_ADDRESS_STREET_NAME";
    protected static final String $PATIENT_ADDRESS_STREET_NUMBER = "$PATIENT_ADDRESS_STREET_NUMBER";
    protected static final String $PATIENT_ADDRESS_POSTAL_CODE = "$PATIENT_ADDRESS_POSTAL_CODE";
    protected static final String $PATIENT_ADDRESS_CITY = "$PATIENT_ADDRESS_CITY";
    protected static final String $PATIENT_STATUS = "$PATIENT_STATUS";
    protected static final String $PRACTITIONER_ID = "$PRACTITIONER_ID";
    protected static final String $PRACTITIONER_NAME_PREFIX = "$PRACTITIONER_NAME_PREFIX";
    protected static final String $PRACTITIONER_NAME_FIRST = "$PRACTITIONER_NAME_FIRST";
    protected static final String $PRACTITIONER_NAME_FAMILY = "$PRACTITIONER_NAME_FAMILY";
    protected static final String $PRACTITIONER_ADDRESS_STREET_NAME = "$PRACTITIONER_ADDRESS_STREET_NAME";
    protected static final String $PRACTITIONER_ADDRESS_STREET_NUMBER = "$PRACTITIONER_ADDRESS_STREET_NUMBER";
    protected static final String $PRACTITIONER_ADDRESS_POSTAL_CODE = "$PRACTITIONER_ADDRESS_POSTAL_CODE";
    protected static final String $PRACTITIONER_ADDRESS_CITY = "$PRACTITIONER_ADDRESS_CITY";
    protected static final String $PRACTITIONER_PHONE = "$PRACTITIONER_PHONE";
    protected static final String $PRACTITIONER_FAX = "$PRACTITIONER_FAX";
    protected static final String $PRACTITIONER_QUALIFICATIONS = "$PRACTITIONER_QUALIFICATIONS";
    protected static final String $PRACTITIONER_NUMBER = "$PRACTITIONER_NUMBER";
    protected static final String $PRACTITIONER_QUALIFICATION_TEXT = "$PRACTITIONER_QUALIFICATION_TEXT";
    protected static final String $ORGANIZATION_ID = "$ORGANIZATION_ID";
    protected static final String $CLINIC_ID = "$CLINIC_ID";
    protected static final String $ORGANIZATION_NAME = "$ORGANIZATION_NAME";
    protected static final String $ORGANIZATION_PHONE = "$ORGANIZATION_PHONE";
    protected static final String $ORGANIZATION_ADDRESS_LINE = "$ORGANIZATION_ADDRESS_LINE";
    protected static final String $ORGANIZATION_ADDRESS_STREET_NAME = "$ORGANIZATION_ADDRESS_STREET_NAME";
    protected static final String $ORGANIZATION_ADDRESS_STREET_NUMBER = "$ORGANIZATION_ADDRESS_STREET_NUMBER";
    protected static final String $ORGANIZATION_ADDRESS_POSTAL_CODE = "$ORGANIZATION_ADDRESS_POSTAL_CODE";
    protected static final String $ORGANIZATION_ADDRESS_CITY = "$ORGANIZATION_ADDRESS_CITY";
    protected static final String $ORGANIZATION_FAX = "$ORGANIZATION_FAX";
    protected static final String $COVERAGE_ID = "$COVERAGE_ID";
    protected static final String $COVERAGE_PERIOD_END = "$COVERAGE_PERIOD_END";
    protected static final String $INSURANCE_NAME = "$INSURANCE_NAME";
    protected static final String $INSURANCE_NUMBER = "$INSURANCE_NUMBER";

    protected Map<String, Pair<Boolean, Integer>> templateKeyMapper;
    protected String jsonTemplateForBundle;
    protected FhirContext ctx = FhirContext.forR4();
    protected IParser jsonParser = ctx.newJsonParser();

    public PrescriptionBundlesBuilderV2(Muster16PrescriptionForm muster16PrescriptionForm) {
        super(muster16PrescriptionForm);
    }

    //TODO: Use the templateKeyMapper Map to implement reconciliation and tracking of values
    // injected into template.
    protected void initTemplateKeyMapper() {
        templateKeyMapper = new HashMap<>();
        
        templateKeyMapper.put($PRESCRIPTION_ID, Pair.of(true, 0));
        templateKeyMapper.put($BUNDLE_ID, Pair.of(true, 0));
        templateKeyMapper.put($LAST_UPDATED, Pair.of(true, 0));
        templateKeyMapper.put($TIMESTAMP, Pair.of(true, 0));
        templateKeyMapper.put($COMPOSITION_ID, Pair.of(true, 0));
        templateKeyMapper.put($COMPOSITION_DATE, Pair.of(true, 0));
        templateKeyMapper.put($DEVICE_ID, Pair.of(true, 0));
        templateKeyMapper.put($MEDICATION_REQUEST_ID, Pair.of(true, 0));
        templateKeyMapper.put($STATUS_CO_PAYMENT, Pair.of(true, 0));
        templateKeyMapper.put($AUTHORED_ON, Pair.of(true, 0));
        templateKeyMapper.put($MEDICATION_ID, Pair.of(true, 0));
        templateKeyMapper.put($DOSAGE_QUANTITY, Pair.of(false, 0));
        templateKeyMapper.put($PZN, Pair.of(true, 0));
        templateKeyMapper.put($MEDICATION_NAME, Pair.of(true, 0));
        templateKeyMapper.put($MEDICATION_FORM, Pair.of(false, 0));
        templateKeyMapper.put($MEDICATION_SIZE, Pair.of(true, 0));
        templateKeyMapper.put($DOSAGE_TEXT, Pair.of(false, 0));;
        templateKeyMapper.put($KVID_10, Pair.of(false, 0));
        templateKeyMapper.put($PATIENT_ID, Pair.of(true, 0));
        templateKeyMapper.put($PATIENT_NAME_PREFIX, Pair.of(false, 0));
        templateKeyMapper.put($PATIENT_NAME_FIRST, Pair.of(true, 0));
        templateKeyMapper.put($PATIENT_NAME_FAMILY, Pair.of(true, 0));
        templateKeyMapper.put($PATIENT_BIRTH_DATE, Pair.of(true, 0));
        templateKeyMapper.put($PATIENT_ADDRESS_LINE, Pair.of(true, 0));
        templateKeyMapper.put($PATIENT_ADDRESS_STREET_NAME, Pair.of(true, 0));
        templateKeyMapper.put($PATIENT_ADDRESS_STREET_NUMBER, Pair.of(true, 0));
        templateKeyMapper.put($PATIENT_ADDRESS_POSTAL_CODE, Pair.of(true, 0));
        templateKeyMapper.put($PATIENT_ADDRESS_CITY, Pair.of(true, 0));
        templateKeyMapper.put($PATIENT_STATUS, Pair.of(false, 0));
        templateKeyMapper.put($PRACTITIONER_ID, Pair.of(true, 0));
        templateKeyMapper.put($PRACTITIONER_NAME_PREFIX, Pair.of(false, 0));
        templateKeyMapper.put($PRACTITIONER_NAME_FIRST, Pair.of(true, 0));
        templateKeyMapper.put($PRACTITIONER_NAME_FAMILY, Pair.of(true, 0));
        templateKeyMapper.put($PRACTITIONER_ADDRESS_STREET_NAME, Pair.of(true, 0));
        templateKeyMapper.put($PRACTITIONER_ADDRESS_STREET_NUMBER, Pair.of(true, 0));
        templateKeyMapper.put($PRACTITIONER_ADDRESS_POSTAL_CODE, Pair.of(true, 0));
        templateKeyMapper.put($PRACTITIONER_ADDRESS_CITY, Pair.of(true, 0));
        templateKeyMapper.put($PRACTITIONER_PHONE, Pair.of(false, 0));
        templateKeyMapper.put($PRACTITIONER_FAX, Pair.of(false, 0));
        templateKeyMapper.put($PRACTITIONER_QUALIFICATIONS, Pair.of(true, 0));
        templateKeyMapper.put($PRACTITIONER_NUMBER, Pair.of(true, 0));
        templateKeyMapper.put($PRACTITIONER_QUALIFICATION_TEXT, Pair.of(false, 0));
        templateKeyMapper.put($ORGANIZATION_ID, Pair.of(true, 0));
        templateKeyMapper.put($CLINIC_ID, Pair.of(true, 0));
        templateKeyMapper.put($ORGANIZATION_PHONE, Pair.of(false, 0));
        templateKeyMapper.put($ORGANIZATION_NAME, Pair.of(false, 0));
        templateKeyMapper.put($ORGANIZATION_ADDRESS_LINE, Pair.of(false, 0));
        templateKeyMapper.put($ORGANIZATION_ADDRESS_STREET_NAME, Pair.of(true, 0));
        templateKeyMapper.put($ORGANIZATION_ADDRESS_STREET_NUMBER, Pair.of(true, 0));
        templateKeyMapper.put($ORGANIZATION_ADDRESS_POSTAL_CODE, Pair.of(true, 0));
        templateKeyMapper.put($ORGANIZATION_ADDRESS_CITY, Pair.of(true, 0));
        templateKeyMapper.put($ORGANIZATION_PHONE, Pair.of(false, 0));
        templateKeyMapper.put($ORGANIZATION_FAX, Pair.of(false, 0));
        templateKeyMapper.put($COVERAGE_ID, Pair.of(true, 0));
        templateKeyMapper.put($COVERAGE_PERIOD_END, Pair.of(true, 0));
        templateKeyMapper.put($INSURANCE_NAME, Pair.of(true, 0));
        templateKeyMapper.put($INSURANCE_NUMBER, Pair.of(true, 0));
    }

    @Override
    public Bundle createBundleForMedication(MedicationString medication) {
        Bundle bundle;

        try(InputStream is = getClass().getResourceAsStream(
                "/bundle-samples/FEbundleTemplate.json")) {
            jsonTemplateForBundle = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            updateBundleResourceSection();
            updateCompositionSection();
            updateMedicationRequestSection();
            updateMedicationResourceSection(medication);
            updatePatientResourceSection();
            updatePractitionerResourceSection();
            updateOrganizationResourceSection();
            updateCoverageResourceSection();

            bundle = jsonParser.parseResource(Bundle.class, jsonTemplateForBundle);

            bundle.getMeta().setLastUpdated(new Date());
            bundle.setTimestamp(new Date());
            ((Composition)bundle.getEntry().get(0).getResource()).setDate(new Date());
            ((MedicationRequest)bundle.getEntry().get(1).getResource()).getMeta().setLastUpdated(
                    new Date());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bundle template or sample!", e);
        }

        return bundle;
    }

    protected void updateBundleResourceSection() {
        jsonTemplateForBundle = jsonTemplateForBundle.replace($BUNDLE_ID,
                StringUtils.defaultString(UUID.randomUUID().toString()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRESCRIPTION_ID,
                StringUtils.defaultString(UUID.randomUUID().toString()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($LAST_UPDATED,
                StringUtils.defaultString(null));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($TIMESTAMP,
                StringUtils.defaultString(null));
    }

    protected void updateCompositionSection() {
        jsonTemplateForBundle = jsonTemplateForBundle.replace($COMPOSITION_ID,
                StringUtils.defaultString(UUID.randomUUID().toString()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($COMPOSITION_DATE,
                StringUtils.defaultString(null));

        //TODO: This is hardcoded but updated values may need to be sourced dynamically over time.
        jsonTemplateForBundle = jsonTemplateForBundle.replace($DEVICE_ID,
                StringUtils.defaultString("Y/410/2107/36/999"));
    }

    protected void updateMedicationRequestSection() {
        jsonTemplateForBundle = jsonTemplateForBundle.replace($MEDICATION_REQUEST_ID,
                StringUtils.defaultString(UUID.randomUUID().toString()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($STATUS_CO_PAYMENT,
                StringUtils.defaultString(
                        muster16PrescriptionForm.getWithPayment() ? "0" : "1", null));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($AUTHORED_ON,
                StringUtils.defaultString(muster16PrescriptionForm.getPrescriptionDate()));
    }

    protected void updateMedicationResourceSection(MedicationString medicationString) {
        jsonTemplateForBundle = jsonTemplateForBundle.replace($MEDICATION_ID,
                StringUtils.defaultString(UUID.randomUUID().toString()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($DOSAGE_QUANTITY,
                StringUtils.defaultString(medicationString.getDosage()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PZN,
                StringUtils.defaultString(medicationString.getPzn()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($MEDICATION_FORM,
                StringUtils.defaultString(medicationString.getForm()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($MEDICATION_NAME,
                StringUtils.defaultString(medicationString.getName()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($MEDICATION_SIZE,
                StringUtils.defaultString(medicationString.getSize()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($DOSAGE_TEXT,
                StringUtils.defaultString(medicationString.getInstructions()));
    }

    protected void updatePatientResourceSection() {
        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_ID,
                StringUtils.defaultString(UUID.randomUUID().toString()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($KVID_10,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientInsuranceId()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_NAME_PREFIX,
                StringUtils.defaultString(
                        CollectionUtils.isNotEmpty(muster16PrescriptionForm.getPatientNamePrefix())?
                         muster16PrescriptionForm.getPatientNamePrefix().stream().findFirst().get() :
                                ""));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_NAME_FIRST,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientFirstName()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_NAME_FAMILY,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientLastName()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_BIRTH_DATE,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientDateOfBirth()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_ADDRESS_LINE,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientStreetName()) +
                " " + muster16PrescriptionForm.getPatientStreetNumber());

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_ADDRESS_STREET_NAME,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientStreetName()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_ADDRESS_STREET_NUMBER,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientStreetNumber()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_ADDRESS_POSTAL_CODE,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientZipCode()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_ADDRESS_CITY,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientCity()));
    }

    protected void updatePractitionerResourceSection() {
        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_ID,
                StringUtils.defaultString(UUID.randomUUID().toString()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_NAME_PREFIX,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerNamePrefix()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_NAME_FIRST,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerFirstName()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_NAME_FAMILY,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerLastName()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_ADDRESS_STREET_NAME,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerStreetName()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_ADDRESS_STREET_NUMBER,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerStreetNumber()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_ADDRESS_POSTAL_CODE,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerZipCode()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_ADDRESS_CITY,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerCity()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_PHONE,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerPhone()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_FAX,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerFax()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_QUALIFICATIONS,
                StringUtils.defaultString(null));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_NUMBER,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerId()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PRACTITIONER_QUALIFICATION_TEXT,
                StringUtils.defaultString(null));
    }

    protected void updateOrganizationResourceSection() {
        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_ID,
                UUID.randomUUID().toString());

        jsonTemplateForBundle = jsonTemplateForBundle.replace($CLINIC_ID,
                muster16PrescriptionForm.getClinicId());

        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_NAME,
                StringUtils.defaultString(null));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_PHONE,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerPhone()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_ADDRESS_LINE,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerStreetName() +
                        " " + muster16PrescriptionForm.getPractitionerStreetNumber()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_ADDRESS_STREET_NAME,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerStreetName()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_ADDRESS_STREET_NUMBER,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerStreetNumber()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_ADDRESS_POSTAL_CODE,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerZipCode()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_ADDRESS_CITY,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerCity()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_PHONE,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerPhone()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($ORGANIZATION_FAX,
                StringUtils.defaultString(muster16PrescriptionForm.getPractitionerFax()));
    }

    protected void updateCoverageResourceSection() {
        jsonTemplateForBundle = jsonTemplateForBundle.replace($COVERAGE_ID,
                StringUtils.defaultString(UUID.randomUUID().toString()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($COVERAGE_PERIOD_END,
                StringUtils.defaultString(muster16PrescriptionForm.getPrescriptionDate()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($INSURANCE_NAME,
                StringUtils.defaultString(muster16PrescriptionForm.getInsuranceCompany()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($PATIENT_STATUS,
                StringUtils.defaultString(muster16PrescriptionForm.getPatientStatus()));

        jsonTemplateForBundle = jsonTemplateForBundle.replace($INSURANCE_NUMBER,
                StringUtils.defaultString(muster16PrescriptionForm.getInsuranceCompanyId()));
    }
}
