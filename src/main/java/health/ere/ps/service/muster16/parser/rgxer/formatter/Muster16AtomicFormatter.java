package health.ere.ps.service.muster16.parser.rgxer.formatter;

import health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Muster16AtomicFormatter {

    private final Pattern EXTRA_WHITE_SPACE = Pattern.compile("\\s+");
    private final Pattern NUMBERS = Pattern.compile("(\\d+)", Pattern.DOTALL);
    private final Pattern DATE = Pattern.compile("\\d+[.-/]\\d+[.-/]\\d+");
    private final Pattern SHORT_ORDINAL_DATE = Pattern.compile("(?<day>\\d+)\\.(?<month>\\d+)\\.(?<year>\\d+)");

    private final DateTimeFormatter ORDINAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter SHORT_ORDINAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy");
    private final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


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

    private LocalDate parseDate(String entry) {
        LocalDate date;
        if (matches(entry, SHORT_ORDINAL_DATE_FORMAT) != null)
            return parseShortOrdinalDate(entry);
        else if ((date = matches(entry, ORDINAL_DATE_FORMAT)) != null)
            return date;
        else
            return null;
    }

    private String reformatDate(String entry) {
        LocalDate date = parseDate(entry);
        return date != null ? STANDARD_DATE_FORMAT.format(date) : null;
    }

    private String formatInsuranceCompany(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    private String formatInsuranceCompanyId(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    private String formatPatientNamePrefix(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    private String formatPatientFirstName(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    private String formatPatientLastName(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    private String formatPatientStreetName(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    private String formatPatientStreetNumber(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    private String formatPatientCity(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    private String formatPatientZipcode(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    private String formatPatientDateOfBirth(String entry) {
        return FormattingChain.format(entry).apply(s -> cleanNoise(s, DATE)).apply(this::reformatDate).get();
    }

    private String formatClinicId(String entry) {
        return FormattingChain.format(entry).apply(s -> cleanNoise(s, NUMBERS)).get();
    }

    private String formatDoctorId(String entry) {
        return FormattingChain.format(entry).apply(s -> cleanNoise(s, NUMBERS)).get();
    }

    private String formatPrescriptionDate(String entry) {
        return FormattingChain.format(entry).apply(s -> cleanNoise(s, DATE)).apply(this::reformatDate).get();
    }

    private String formatPatientInsuranceId(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    String format(Muster16Field key, String value) {
        switch (key) {
            case INSURANCE_COMPANY:
                return formatInsuranceCompany(value);
            case INSURANCE_COMPANY_ID:
                return formatInsuranceCompanyId(value);
            case PATIENT_NAME_PREFIX:
                return formatPatientNamePrefix(value);
            case PATIENT_FIRST_NAME:
                return formatPatientFirstName(value);
            case PATIENT_LAST_NAME:
                return formatPatientLastName(value);
            case PATIENT_STREET_NAME:
                return formatPatientStreetName(value);
            case PATIENT_STREET_NUMBER:
                return formatPatientStreetNumber(value);
            case PATIENT_CITY:
                return formatPatientCity(value);
            case PATIENT_ZIPCODE:
                return formatPatientZipcode(value);
            case PATIENT_DATE_OF_BIRTH:
                return formatPatientDateOfBirth(value);
            case CLINIC_ID:
                return formatClinicId(value);
            case DOCTOR_ID:
                return formatDoctorId(value);
            case PRESCRIPTION_DATE:
                return formatPrescriptionDate(value);
            case PRESCRIPTION_LIST:
                return null;
            case PATIENT_INSURANCE_ID:
                return formatPatientInsuranceId(value);
            default:
                return null;
        }
    }

    public Map<Muster16Field, String> format(Map<Muster16Field, String> entries) {
        return entries.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> format(e.getKey(), e.getValue())));
    }
}
