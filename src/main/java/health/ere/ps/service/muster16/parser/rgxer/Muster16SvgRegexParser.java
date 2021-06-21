package health.ere.ps.service.muster16.parser.rgxer;

import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.service.muster16.parser.IMuster16FormParser;
import health.ere.ps.service.muster16.parser.rgxer.delegate.medication.MedicationParseDelegate;
import health.ere.ps.service.muster16.parser.rgxer.delegate.patient.PatientEntryParseDelegate;
import health.ere.ps.service.muster16.parser.rgxer.delegate.practitioner.PractitionerEntryParseDelegate;
import health.ere.ps.service.muster16.parser.rgxer.formatter.Muster16AtomicFormatter;
import health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field;

import java.util.*;
import java.util.stream.Collectors;

import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.*;


public class Muster16SvgRegexParser implements IMuster16FormParser {

    private final Map<Muster16Field, String> atomicResult;
    private final List<MedicationString> prescribedMedication;


    public Muster16SvgRegexParser(Map<String, String> entries) {
        atomicResult = new HashMap<>();
        atomicResult.putAll(parseAtomicFields(entries));

        prescribedMedication = new ArrayList<>();
        prescribedMedication.addAll(parseMedication(entries.get("medication")));
    }

    private Map<Muster16Field, String> parseAtomicFields(Map<String, String> entries) {
        Map<Muster16Field, String> mappedFields = mapFields(entries);
        return formatValues(mappedFields);
    }

    private Map<Muster16Field, String> extractPatientAndPractitionerValues(Map<String, String> mappedValues) {
        Map<Muster16Field, String> patientAndPractitionerDetailsFields = new PatientEntryParseDelegate(
                mappedValues.getOrDefault("nameAndAddress", "")).getDetails();

        patientAndPractitionerDetailsFields.putAll(new PractitionerEntryParseDelegate((
                mappedValues.getOrDefault("practitionerText", ""))).getDetails());

        return patientAndPractitionerDetailsFields;
    }

    private Map<Muster16Field, String> formatValues(Map<Muster16Field, String> mappedFields) {
        Muster16AtomicFormatter formatter = new Muster16AtomicFormatter();
        return formatter.format(mappedFields);
    }

    private Map<Muster16Field, String> mapFields(Map<String, String> entries) {
        Map<Muster16Field, String> fieldsMap = new HashMap<>();
        fieldsMap.put(INSURANCE_COMPANY, entries.getOrDefault("insurance", ""));
        fieldsMap.put(INSURANCE_COMPANY_ID, entries.getOrDefault("payor", ""));
        fieldsMap.put(PATIENT_DATE_OF_BIRTH, entries.getOrDefault("birthdate", ""));
        fieldsMap.put(CLINIC_ID, entries.getOrDefault("locationNumber", ""));
        fieldsMap.put(DOCTOR_ID, entries.getOrDefault("practitionerNumber", ""));
        fieldsMap.put(PRESCRIPTION_DATE, entries.getOrDefault("date", ""));
        fieldsMap.put(PATIENT_INSURANCE_ID, entries.getOrDefault("insuranceNumber", ""));
        fieldsMap.put(IS_WITH_PAYMENT, entries.getOrDefault("withPayment", ""));
        fieldsMap.putAll(extractPatientAndPractitionerValues(entries));
        return fieldsMap;
    }

    private List<MedicationString> parseMedication(String medication) {
        MedicationParseDelegate parser = new MedicationParseDelegate();
        return parser.parse(medication);
    }

    private String getValue(Muster16Field key) {
        String defaultValue = "";
        String value = atomicResult.getOrDefault(key, defaultValue);
        return value != null ? value : defaultValue;
    }

    @Override
    public String parseInsuranceCompany() {
        return getValue(INSURANCE_COMPANY);
    }

    @Override
    public String parseInsuranceCompanyId() {
        return getValue(INSURANCE_COMPANY_ID);
    }

    @Override
    public List<String> parsePatientNamePrefix() {
        String value = getValue(PATIENT_NAME_PREFIX);
        if(value.isBlank())
            return Collections.emptyList();
        return List.of(value.split(" ")).stream().map(String::new).collect(Collectors.toList());
    }

    @Override
    public String parsePatientFirstName() {
        return getValue(PATIENT_FIRST_NAME);
    }

    @Override
    public String parsePatientLastName() {
        return getValue(PATIENT_LAST_NAME);
    }

    @Override
    public String parsePatientStreetName() {
        return getValue(PATIENT_STREET_NAME);
    }

    @Override
    public String parsePatientStreetNumber() {
        return getValue(PATIENT_STREET_NUMBER);
    }

    @Override
    public String parsePatientCity() {
        return getValue(PATIENT_CITY);
    }

    @Override
    public String parsePatientZipCode() {
        return getValue(PATIENT_ZIPCODE);
    }

    @Override
    public String parsePatientDateOfBirth() {
        return getValue(PATIENT_DATE_OF_BIRTH);
    }

    @Override
    public String parseClinicId() {
        return getValue(CLINIC_ID);
    }

    @Override
    public String parseDoctorId() {
        return getValue(DOCTOR_ID);
    }

    @Override
    public String parsePrescriptionDate() {
        return getValue(PRESCRIPTION_DATE);
    }

    @Override
    public List<MedicationString> parsePrescriptionList() {
        return prescribedMedication;
    }

    @Override
    public String parsePatientInsuranceId() {
        return getValue(PATIENT_INSURANCE_ID);
    }

    @Override
    public Boolean parseIsWithPayment() {
        return getValue(IS_WITH_PAYMENT).equals("X");
    }

    @Override
    public String parsePractitionerFirstName() {
        return getValue(PRACTITIONER_FIRST_NAME);
    }

    @Override
    public String parsePractitionerLastName() {
        return getValue(PRACTITIONER_LAST_NAME);
    }

    @Override
    public String parsePractitionerNamePrefix() {
        return getValue(PRACTITIONER_NAME_PREFIX);
    }

    @Override
    public String parsePractitionerStreetName() {
        return getValue(PRACTITIONER_STREET_NAME);
    }

    @Override
    public String parsePractitionerStreetNumber() {
        return getValue(PRACTITIONER_STREET_NUMBER);
    }

    @Override
    public String parsePractitionerCity() {
        return getValue(PRACTITIONER_CITY);
    }

    @Override
    public String parsePractitionerZipCode() {
        return getValue(PRACTITIONER_ZIPCODE);
    }

    @Override
    public String parsePractitionerPhoneNumber() {
        return getValue(PRACTITIONER_PHONE);
    }

    @Override
    public String parsePractitionerFaxNumber() {
        return getValue(PRACTITIONER_FAX);
    }
}
