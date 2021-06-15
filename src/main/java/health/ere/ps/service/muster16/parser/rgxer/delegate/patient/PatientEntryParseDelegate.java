package health.ere.ps.service.muster16.parser.rgxer.delegate.patient;

import health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field;
import health.ere.ps.service.muster16.parser.rgxer.delegate.pattern.PatientPatterns;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.*;

public class PatientEntryParseDelegate {

    private final Map<Muster16Field, String> details;
    private final PatientPatterns patterns;

    public PatientEntryParseDelegate(String entry) {
        details = new HashMap<>();
        patterns = new PatientPatterns();
        extract(entry);
    }

    public Map<Muster16Field, String> getDetails() {
        return details;
    }

    private void extract(String entry) {
        List<String> lines = Arrays.stream(entry.split("\\n"))
                .map(String::trim)
                .collect(Collectors.toList());

        if (lines.size() >= 4) {
            matchAndExtractLine(lines, patterns.ADDRESS_LINE).ifPresent(this::parseAddressLine);
            matchAndExtractLine(lines, patterns.STREET_LINE).ifPresent(this::parseStreetLine);
            matchAndExtractLine(lines, patterns.FIRST_NAME_LINE).ifPresentOrElse(this::parseFirstName, () -> parseFirstName(lines.get(1)));
            parseLastName(lines.get(0));
        }
    }

    private void parseLastName(String token) {
        details.put(PATIENT_LAST_NAME, token);
    }

    private void parseFirstName(String entry) {
        parseNamePrefix(entry);
        entry = entry.replaceAll(patterns.NAME_PREFIX.pattern(), "");
        details.put(PATIENT_FIRST_NAME, entry);
    }

    private void parseNamePrefix(String entry) {
        Matcher matcher = patterns.NAME_PREFIX.matcher(entry);
        StringBuilder builder = new StringBuilder();
        while (matcher.find())
            builder.append(matcher.group());
        details.put(PATIENT_NAME_PREFIX, builder.toString());
    }

    private void parseAddressLine(String line) {
        Matcher matcher = patterns.ADDRESS_LINE.matcher(line);
        if (matcher.matches()) {
            details.put(PATIENT_ZIPCODE, matcher.group(2));
            details.put(PATIENT_CITY, matcher.group(3));
        }
    }

    private void parseStreetLine(String line) {
        Matcher matcher = patterns.STREET_LINE.matcher(line);
        if (matcher.matches()) {
            details.put(PATIENT_STREET_NAME, matcher.group(1));
            details.put(PATIENT_STREET_NUMBER, matcher.group(2));
        }
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
}
