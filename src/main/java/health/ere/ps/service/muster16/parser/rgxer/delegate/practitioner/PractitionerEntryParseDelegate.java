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
            matchAndExtractLine(lines, patterns.FAX_LINE).ifPresent(this::parseFaxNumber);
            matchAndExtractLine(lines, patterns.PHONE_LINE).ifPresent(this::parsePhoneNumber);
            matchAndExtractLine(lines, patterns.CITY_LINE).ifPresent(this::parseAddressLine);
            matchAndExtractLine(lines, patterns.STREET_LINE).ifPresent(this::parseStreetLine);
            matchAndExtractLine(lines, patterns.NAME_LINE).ifPresent(this::parseNames);
        }
    }

    private void parseNames(String entry) {
        parseNamePrefix(entry);
        entry = entry.replaceAll(patterns.NAME_PREFIX.pattern(), "");
        String[] names = entry.split(" ");
        details.put(PRACTITIONER_LAST_NAME, names[names.length - 1]);
        details.put(PRACTITIONER_FIRST_NAME, entry.substring(0, entry.lastIndexOf(" ")));
    }

    private void parseNamePrefix(String entry) {
        Matcher matcher = patterns.NAME_PREFIX.matcher(entry);
        StringBuilder builder = new StringBuilder();
        while (matcher.find())
            builder.append(matcher.group());
        details.put(PRACTITIONER_NAME_PREFIX, builder.toString());
    }

    private void parseAddressLine(String line) {
        String[] splitLine = line.split(" ");
        if (splitLine[0].matches("[0-9]+")) {
            details.put(PRACTITIONER_ZIPCODE, splitLine[0]);
            line = line.replace(splitLine[0], "").trim();
        } else {
            details.put(PRACTITIONER_ZIPCODE, splitLine[splitLine.length - 1]);
            line = line.replace(splitLine[splitLine.length - 1], "").trim();
        }
        details.put(PRACTITIONER_CITY, line);
    }

    private void parseStreetLine(String line) {
        String streetNumber = line.replaceAll("\\D+", "");

        details.put(PRACTITIONER_STREET_NUMBER, streetNumber);
        details.put(PRACTITIONER_STREET_NAME, line.replace(streetNumber, "").trim());
    }

    private void parsePhoneNumber(String line) {
        details.put(PRACTITIONER_PHONE, line.replaceAll("\\D+", "").trim());
    }

    private void parseFaxNumber(String line) {
        details.put(PRACTITIONER_FAX, line.replaceAll("\\D+", "").trim());
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
