package health.ere.ps.service.muster16.parser.rgxer.extractor;

import health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.*;

public class PatientDetailsIntermediateExtractor {

    final Pattern NAME_PREFIX = Pattern.compile("(Prof|Dr)\\.");
    final Pattern FIRST_NAME_LINE = Pattern.compile("(?<prefix>(Prof|Dr)\\.)(.*)");
    final Pattern ADDRESS_LINE = Pattern.compile("(.*)(\\d{5})(.*)");
    final Pattern STREET_LINE = Pattern.compile("(\\D+)(\\d+)");

    private final Map<Muster16Field, String> details;

    public PatientDetailsIntermediateExtractor(String entry) {
        details = new HashMap<>();
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
            matchAndExtractLine(lines, ADDRESS_LINE).ifPresent(this::parseAddressLine);
            matchAndExtractLine(lines, STREET_LINE).ifPresent(this::parseStreetLine);
            matchAndExtractLine(lines, FIRST_NAME_LINE).ifPresentOrElse(this::parseFirstName, () -> parseFirstName(lines.get(1)));
            parseLastName(lines.get(0));
        }
    }

    private void parseLastName(String token) {
        details.put(PATIENT_LAST_NAME, token);
    }

    private void parseFirstName(String entry) {
        parseNamePrefix(entry);
        entry = entry.replaceAll(NAME_PREFIX.pattern(), "");
        details.put(PATIENT_FIRST_NAME, entry);
    }

    private void parseNamePrefix(String entry) {
        Matcher matcher = NAME_PREFIX.matcher(entry);
        StringBuilder builder = new StringBuilder();
        while (matcher.find())
            builder.append(matcher.group());
        details.put(PATIENT_NAME_PREFIX, builder.toString());
    }

    private void parseAddressLine(String line) {
        Matcher matcher = ADDRESS_LINE.matcher(line);
        if (matcher.matches()) {
            details.put(PATIENT_ZIPCODE, matcher.group(2));
            details.put(PATIENT_CITY, matcher.group(3));
        }
    }

    private void parseStreetLine(String line) {
        Matcher matcher = STREET_LINE.matcher(line);
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
