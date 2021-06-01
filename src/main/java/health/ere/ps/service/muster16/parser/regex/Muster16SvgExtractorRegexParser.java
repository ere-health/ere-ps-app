package health.ere.ps.service.muster16.parser.regex;

import health.ere.ps.service.muster16.parser.IMuster16FormParser;
import health.ere.ps.model.muster16.MedicationString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



public class Muster16SvgExtractorRegexParser implements IMuster16FormParser {

    private Map<String, String> parsedValues;

    final Pattern EXTRA_WHITE_SPACE = Pattern.compile("\\s+");
    final Pattern NUMBERS = Pattern.compile("(\\d+)", Pattern.DOTALL);
    final Pattern NAME_PREFIX = Pattern.compile("(Prof|Dr)\\.");
    final Pattern FIRST_NAME_LINE = Pattern.compile("(?<prefix>(Prof|Dr)\\.)(.*)");
    final Pattern ADDRESS_LINE = Pattern.compile("(.*)(\\d{5})(.*)");
    final Pattern STREET_LINE = Pattern.compile("(\\D+)(\\d+)");
    final Pattern DATE = Pattern.compile("\\d+[.-/]\\d+[.-/]\\d+");
    private final Pattern SHORT_ORDINAL_DATE = Pattern.compile("(?<day>\\d+)\\.(?<month>\\d+)\\.(?<year>\\d+)");

    final DateTimeFormatter ORDINAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    final DateTimeFormatter SHORT_ORDINAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy");
    final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public Muster16SvgExtractorRegexParser(Map<String, String> mappedFields) {
        parseValues(mappedFields);
    }

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
        if (lines.size() >= 4) {
            matchAndExtractLine(lines, ADDRESS_LINE).ifPresent(this::parseAddressLine);
            matchAndExtractLine(lines, STREET_LINE).ifPresent(this::parseStreetLine);
            matchAndExtractLine(lines, FIRST_NAME_LINE).ifPresentOrElse(this::parseFirstName, () -> parseFirstName(lines.get(1)));
            parseLastName(lines.get(0));
        }
    }

    private void parseLastName(String token) {
        parsedValues.put("patientLastName", cleanToken(token));
    }

    private void parseFirstName(String entry) {
        parseFirstNamePrefix(entry);
        entry = entry.replaceAll(NAME_PREFIX.pattern(),"");
        parsedValues.put("patientFirstName", cleanToken(entry));
    }

    private void parseFirstNamePrefix(String entry) {
        Matcher matcher = NAME_PREFIX.matcher(entry);
        StringBuilder builder = new StringBuilder();
        while (matcher.find())
            builder.append(matcher.group());
        parsedValues.put("patientNamePrefix", cleanToken(builder.toString()));
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

    private void parseBirthdate(String entry) {
        String cleaned = cleanNoise(entry, DATE);
        parsedValues.put("birthdate", reformatDate(cleaned));
    }

    private void parseClinicId(String entry) {
        parsedValues.put("clinicId", cleanNoise(entry, NUMBERS));
    }

    private void parsePractitionerNumber(String entry) {
        parsedValues.put("practitionerNumber", cleanNoise(entry, NUMBERS));
    }

    private void parsePrescriptionDate(String entry) {
        String cleaned = cleanNoise(entry, DATE);
        parsedValues.put("date", reformatDate(cleaned));
    }

    private void parsePatientInsuranceId(String entry) {
        parsedValues.put("insuranceNumber", cleanToken(entry));
    }

    private String getValue(String key) {
        String defaultValue = "";
        String value = parsedValues.getOrDefault(key, defaultValue);
        return value != null ? value : defaultValue;
    }

    @Override
    public String parseInsuranceCompany() {
        return getValue("insuranceCompany");
    }

    @Override
    public String parseInsuranceCompanyId() {
        return getValue("insuranceCompanyId");
    }

    @Override
    public String parsePatientNamePrefix() {
        return getValue("patientNamePrefix");
    }

    @Override
    public String parsePatientFirstName() {
        return getValue("patientFirstName");
    }

    @Override
    public String parsePatientLastName() {
        return getValue("patientLastName");
    }

    @Override
    public String parsePatientStreetName() {
        return getValue("patientStreetName");
    }

    @Override
    public String parsePatientStreetNumber() {
        return getValue("patientStreetNumber");
    }

    @Override
    public String parsePatientCity() {
        return getValue("patientCity");
    }

    @Override
    public String parsePatientZipCode() {
        return getValue("patientZipCode");
    }

    @Override
    public String parsePatientDateOfBirth() {
        return getValue("birthdate");
    }

    @Override
    public String parseClinicId() {
        return getValue("clinicId");
    }

    @Override
    public String parseDoctorId() {
        return getValue("practitionerNumber");
    }

    @Override
    public String parsePrescriptionDate() {
        return getValue("date");
    }

    @Override
    public List<MedicationString> parsePrescriptionList() {
        return new ArrayList<>();
    }

    @Override
    public String parsePatientInsuranceId() {
        return getValue("insuranceNumber");
    }

    //region Parsing-Utils
    String removeExtraSpaces(String entry) {
        return EXTRA_WHITE_SPACE.matcher(entry).replaceAll(" ").trim();
    }

    String cleanToken(String entry) {
        return removeExtraSpaces(entry);
    }

    String cleanNoise(String entry, Pattern pattern) {
        Matcher matcher = pattern.matcher(entry);
        return matcher.find() ? matcher.group(0) : cleanToken(entry);
    }

    boolean matches(String input, Pattern pattern) {
        return pattern.matcher(input).matches();
    }

    Optional<String> matchAndExtractLine(List<String> lines, Pattern pattern) {

        OptionalInt indexOpt = IntStream.range(0, lines.size())
                .filter(i -> matches(lines.get(i), pattern))
                .findFirst();

        if (indexOpt.isPresent())
            return Optional.of(lines.remove(indexOpt.getAsInt()));
        else
            return Optional.empty();
    }

    private int calculateTargetYear(int targetId) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentId = currentYear % 100;
        return currentYear - currentId - 100 * (targetId > currentId + 1 ? 1 : 0) + targetId;
    }

    private LocalDate parseShortOrdinalDate(String entry) {
        Matcher matcher = SHORT_ORDINAL_DATE.matcher(entry);
        if (matcher.matches()) {
            int day = Integer.parseInt(matcher.group("day")),
                    month = Integer.parseInt(matcher.group("month")),
                    year = calculateTargetYear(Integer.parseInt(matcher.group("year")));
            return LocalDate.of(year, month, day);
        }
        return null;
    }

    private LocalDate matches(String input, DateTimeFormatter format) {
        try {
            return LocalDate.parse(input, format);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    LocalDate parseDate(String entry) {
        LocalDate date;
        if (matches(entry, SHORT_ORDINAL_DATE_FORMAT) != null)
            return parseShortOrdinalDate(entry);
        else if ((date = matches(entry, ORDINAL_DATE_FORMAT)) != null)
            return date;
        else
            return null;
    }

    public String reformatDate(String entry) {
        LocalDate date = parseDate(entry);
        return date != null ? STANDARD_DATE_FORMAT.format(date) : null;
    }
    //endregion
}
