package health.ere.ps.service.muster16.parser.rgxer.delegate.practitioner;

import health.ere.ps.service.muster16.parser.rgxer.delegate.pattern.PractitionerPatterns;
import health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static health.ere.ps.service.muster16.parser.rgxer.model.Muster16Field.*;

public class PractitionerEntryParseDelegate {

    private final Map<Muster16Field, String> details;
    private final PractitionerPatterns patterns;

    public PractitionerEntryParseDelegate(String entry) {
        details = new HashMap<>();
        patterns = new PractitionerPatterns();
        extract(entry);
    }

    public Map<Muster16Field, String> getDetails() {
        return details;
    }

    private void extract(String entry) {
        List<String> lines = Arrays.stream(entry.split("\\n"))
                .map(String::trim)
                .collect(Collectors.toList());

        if (lines.size() >= 5) {
            matchAndExtractLine(lines, patterns.NAME_LINE).ifPresentOrElse(this::parseFirstName, () -> parseFirstName(lines.get(1)));
            matchAndExtractLine(lines, patterns.STREET_LINE).ifPresent(this::parseStreetLine);
            matchAndExtractLine(lines, patterns.CITY_LINE).ifPresent(this::parseAddressLine);
            matchAndExtractLine(lines, patterns.PHONE_LINE).ifPresent(this::parseFirstName);
            matchAndExtractLine(lines, patterns.FAX_LINE).ifPresent(this::parseFirstName);
        }
    }

    private void parseFirstName(String entry) {
        parseNamePrefix(entry);
        entry = entry.replaceAll(patterns.NAME_PREFIX.pattern(), "");
        details.put(PRACTITIONER_FIRST_NAME, entry);
    }

    private void parseNamePrefix(String entry) {
        Matcher matcher = patterns.NAME_PREFIX.matcher(entry);
        StringBuilder builder = new StringBuilder();
        while (matcher.find())
            builder.append(matcher.group());
        details.put(PRACTITIONER_NAME_PREFIX, builder.toString());
    }

    private void parseAddressLine(String line) {
        Matcher matcher = patterns.CITY_LINE.matcher(line);
        if (matcher.matches()) {
            details.put(PRACTITIONER_ZIPCODE, matcher.group(2));
            details.put(PRACTITIONER_CITY, matcher.group(3));
        }
    }

    private void parseStreetLine(String line) {
        Matcher matcher = patterns.STREET_LINE.matcher(line);
        if (matcher.matches()) {
            details.put(PRACTITIONER_STREET_NAME, matcher.group(1));
            details.put(PRACTITIONER_STREET_NUMBER, matcher.group(2));
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
