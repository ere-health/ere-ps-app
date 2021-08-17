package health.ere.ps.service.muster16.parser.rgxer.formatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field;


public class Muster16AtomicFormatter {

    private final static Logger log = Logger.getLogger(Muster16AtomicFormatter.class.getName());

    private final Pattern EXTRA_WHITE_SPACE = Pattern.compile("\\s+");
    private final Pattern NUMBERS = Pattern.compile("(\\d+)", Pattern.DOTALL);
    private final Pattern DATE = Pattern.compile("\\d+[.-/]\\d+[.-/]\\d+");
    private final Pattern SHORT_ORDINAL_DATE = Pattern.compile("(?<day>\\d+)\\.(?<month>\\d+)\\.(?<year>\\d+)");

    private final DateTimeFormatter ORDINAL_DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yyyy");
    private final DateTimeFormatter SHORT_ORDINAL_DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy");
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
        return date != null ? STANDARD_DATE_FORMAT.format(date) : "";
    }

    private String cleanDate(String entry) {
        return FormattingChain.format(entry).apply(s -> cleanNoise(s, DATE)).apply(this::reformatDate).get();
    }

    private String cleanNumber(String entry) {
        return FormattingChain.format(entry).apply(s -> cleanNoise(s, NUMBERS)).get();
    }

    String format(Muster16Field key, String value) {
        try {
            switch (key) {
                case INSURANCE_COMPANY:
                case PATIENT_STREET_NUMBER:
                case PATIENT_STREET_NAME:
                case PATIENT_LAST_NAME:
                case PATIENT_FIRST_NAME:
                case PATIENT_NAME_PREFIX:
                case INSURANCE_COMPANY_ID:
                case PATIENT_CITY:
                case PATIENT_ZIPCODE:
                case PRACTITIONER_CITY:
                case PRACTITIONER_FAX:
                case PRACTITIONER_FIRST_NAME:
                case PRACTITIONER_LAST_NAME:
                case PRACTITIONER_NAME_PREFIX:
                case PRACTITIONER_QUALIFICATION:
                case PRACTITIONER_PHONE:
                case PRACTITIONER_STREET_NAME:
                case PRACTITIONER_STREET_NUMBER:
                case PRACTITIONER_ZIPCODE:
                case PATIENT_INSURANCE_ID:
                case IS_WITH_PAYMENT:
                    return removeExtraSpaces(value);

                case PATIENT_DATE_OF_BIRTH:
                case PRESCRIPTION_DATE:
                    return cleanDate(value);

                case CLINIC_ID:
                case DOCTOR_ID:
                case PATIENT_STATUS:
                    return cleanNumber(value);

                case PRESCRIPTION_LIST:
                    return "";
                default:
                    return "";
            }
        } catch(Throwable t) {
            log.log(Level.SEVERE, "Could not parse muster16 form", t);
            return "";
        }
    }

    public Map<Muster16Field, String> format(Map<Muster16Field, String> entries) {
        return entries.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> format(e.getKey(), e.getValue())));
    }
}
