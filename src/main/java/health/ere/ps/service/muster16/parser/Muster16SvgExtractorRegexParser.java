package health.ere.ps.service.muster16.parser;


import health.ere.ps.model.muster16.MedicationString;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static health.ere.ps.service.muster16.parser.RegexPatterns.*;


public class Muster16SvgExtractorRegexParser implements IMuster16FormParser {

    private Map<String, String> parsedValues;


    public Muster16SvgExtractorRegexParser(Map<String, String> mappedFields) {
        parseValues(mappedFields);
    }

    //region Parse-Utils
    private String removeExtraSpaces(String entry) {
        return EXTRA_WHITE_SPACE.matcher(entry).replaceAll(" ").trim();
    }

    private String cleanToken(String entry) {
        return removeExtraSpaces(entry);
    }

    private String cleanNoise(String entry, Pattern pattern) {
        Matcher matcher = pattern.matcher(entry);
        return matcher.find() ? matcher.group(0) : cleanToken(entry);
    }

    private boolean matches(String input, Pattern pattern) {
        return pattern.matcher(input).matches();
    }

    private Optional<String> matchAndExtractLine(List<String> lines, Pattern pattern) {

        OptionalInt indexOpt = IntStream.range(0, lines.size())
                .filter(i -> matches(lines.get(i), pattern))
                .findFirst();

        if (indexOpt.isPresent())
            return Optional.of(lines.remove(indexOpt.getAsInt()));
        else
            return Optional.empty();
    }
    //endregion

    private void parseValues(Map<String, String> mappedFields) {
        parsedValues = new HashMap<>();
        parseInsuranceCompany(mappedFields.getOrDefault("insurance", ""));
        parseInsuranceCompanyId(mappedFields.getOrDefault("payor", ""));
        parseNameAndAddressField(mappedFields.getOrDefault("nameAndAddress", ""));
        parseBirthdate(mappedFields.getOrDefault("birthdate", ""));
        parseClinicId(mappedFields.getOrDefault("locationNumber", ""));
        parsePractitionerNumber(mappedFields.getOrDefault("practitionerNumber", ""));
        parsePrescriptionDate(mappedFields.getOrDefault("date", ""));
        parsePatientInsuranceId(mappedFields.getOrDefault("insuranceNumber", "A123456789"));
    }

    private void parseInsuranceCompany(String entry) {
        parsedValues.put("insuranceCompany", cleanToken(entry));
    }

    private void parseInsuranceCompanyId(String entry) {
        parsedValues.put("insuranceCompanyId", cleanNoise(entry, NUMBERS));
    }

    //region NameAndAddress-Parsing
    private void parseNameAndAddressField(String nameAndAddress) {
        List<String> lines = Arrays.stream(nameAndAddress.split("\\n"))
                .map(String::trim)
                .collect(Collectors.toList());

        matchAndExtractLine(lines, ADDRESS_LINE).ifPresent(this::parseAddressLine);
        matchAndExtractLine(lines, STREET_LINE).ifPresent(this::parseStreetLine);
        parseFirstName(lines.get(1));
        parseLastName(lines.get(0));
    }

    private void parseLastName(String token) {
        parsedValues.put("patientLastName", cleanToken(token));
    }

    private void parseFirstName(String token) {
        parsedValues.put("patientFirstName", cleanToken(token));
    }

    private void parseAddressLine(String line) {
        Matcher matcher = ADDRESS_LINE.matcher(line);
        if (matcher.matches()) {
            parseZipcode(matcher.group(2));
            parseCity(matcher.group(3));
        }
    }

    private void parseZipcode(String token) {
        parsedValues.put("patientZipCode", cleanToken(token));
    }

    private void parseCity(String token) {
        parsedValues.put("patientCity", cleanToken(token));
    }

    private void parseStreetLine(String line) {
        Matcher matcher = STREET_LINE.matcher(line);
        if (matcher.matches()) {
            parseStreetName(matcher.group(1));
            parseStreetNumber(matcher.group(2));
        }
    }

    private void parseStreetName(String token) {
        parsedValues.put("patientStreetName", cleanToken(token));
    }

    private void parseStreetNumber(String token) {
        parsedValues.put("patientStreetNumber", cleanToken(token));
    }
    //endregion

    private void parseBirthdate(String birthdateEntry) {
        parsedValues.put("birthdate", cleanNoise(birthdateEntry, DATE));
    }

    private void parseClinicId(String entry) {
        parsedValues.put("clinicId", cleanNoise(entry, NUMBERS));
    }

    private void parsePractitionerNumber(String entry) {
        parsedValues.put("practitionerNumber", cleanNoise(entry, NUMBERS));
    }

    private void parsePrescriptionDate(String entry) {
        parsedValues.put("date", cleanNoise(entry, DATE));
    }

    private void parsePatientInsuranceId(String entry) {
        parsedValues.put("insuranceNumber", cleanToken(entry));
    }

    @Override
    public String parseInsuranceCompany() {
        return parsedValues.get("insuranceCompany");
    }

    @Override
    public String parseInsuranceCompanyId() {
        return parsedValues.get("insuranceCompanyId");
    }

    @Override
    public String parsePatientFirstName() {
        return parsedValues.get("patientFirstName");
    }

    @Override
    public String parsePatientLastName() {
        return parsedValues.get("patientLastName");
    }

    @Override
    public String parsePatientStreetName() {
        return parsedValues.get("patientStreetName");
    }

    @Override
    public String parsePatientStreetNumber() {
        return parsedValues.get("patientStreetNumber");
    }

    @Override
    public String parsePatientCity() {
        return parsedValues.get("patientCity");
    }

    @Override
    public String parsePatientZipCode() {
        return parsedValues.get("patientZipCode");
    }

    @Override
    public String parsePatientDateOfBirth() {
        return parsedValues.get("birthdate");
    }

    @Override
    public String parseClinicId() {
        return parsedValues.get("clinicId");
    }

    @Override
    public String parseDoctorId() {
        return parsedValues.get("practitionerNumber");
    }

    @Override
    public String parsePrescriptionDate() {
        return parsedValues.get("date");
    }

    @Override
    public List<MedicationString> parsePrescriptionList() {
        return new ArrayList<>();
    }

    @Override
    public String parsePatientInsuranceId() {
        return parsedValues.get("insuranceNumber");
    }
}
