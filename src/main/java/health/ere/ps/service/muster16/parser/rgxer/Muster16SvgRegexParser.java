package health.ere.ps.service.muster16.parser.rgxer;

import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.service.muster16.parser.IMuster16FormParser;
import health.ere.ps.service.muster16.parser.rgxer.extractor.PatientDetailsIntermediateExtractor;
import health.ere.ps.service.muster16.parser.rgxer.formatter.Muster16AtomicFormatter;
import health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field;

import java.util.*;

import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.*;


public class Muster16SvgRegexParser implements IMuster16FormParser {

    private final Map<Muster16Field, String> result;


    public Muster16SvgRegexParser(Map<String, String> mappedFields) {
        result = new HashMap<>();
        Map<Muster16Field, String> formatted = formatValues(mappedFields);

        result.putAll(formatted);
    }

    private Map<Muster16Field, String> extractIntermediateValues(Map<String, String> mappedValues) {
        Map<Muster16Field, String> patientDetailsFields = new PatientDetailsIntermediateExtractor(mappedValues.getOrDefault("nameAndAddress", "")).getDetails();
        return patientDetailsFields;
    }

    private Map<Muster16Field, String> formatValues(Map<String, String> extractedFields) {
        Map<Muster16Field, String> fieldsMap = buildMap(extractedFields);
        Muster16AtomicFormatter formatter = new Muster16AtomicFormatter();
        return formatter.format(fieldsMap);
    }

    private Map<Muster16Field, String> buildMap(Map<String, String> entries) {
        Map<Muster16Field, String> fieldsMap = new HashMap<>();
        fieldsMap.put(INSURANCE_COMPANY, entries.getOrDefault("insurance", ""));
        fieldsMap.put(INSURANCE_COMPANY_ID, entries.getOrDefault("payor", ""));
        fieldsMap.put(PATIENT_DATE_OF_BIRTH, entries.getOrDefault("birthdate", ""));
        fieldsMap.put(CLINIC_ID, entries.getOrDefault("locationNumber", ""));
        fieldsMap.put(DOCTOR_ID, entries.getOrDefault("practitionerNumber", ""));
        fieldsMap.put(PRESCRIPTION_DATE, entries.getOrDefault("date", ""));
        fieldsMap.put(PATIENT_INSURANCE_ID, entries.getOrDefault("insuranceNumber", ""));
        fieldsMap.putAll(extractIntermediateValues(entries));
        return fieldsMap;
    }

    private String getValue(Muster16Field key) {
        String defaultValue = "";
        String value = result.getOrDefault(key, defaultValue);
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
    public String parsePatientNamePrefix() {
        return getValue(PATIENT_NAME_PREFIX);
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
        return new ArrayList<>();
    }

    @Override
    public String parsePatientInsuranceId() {
        return getValue(PATIENT_INSURANCE_ID);
    }
}
