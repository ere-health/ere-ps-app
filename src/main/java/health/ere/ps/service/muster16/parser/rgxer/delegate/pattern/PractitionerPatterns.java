package health.ere.ps.service.muster16.parser.rgxer.delegate.pattern;

import java.util.regex.Pattern;

public class PractitionerPatterns extends Patterns {
    public final Pattern NAME_PREFIX = Pattern.compile("(Prof|Dr)\\.");
    public final Pattern NAME_LINE = Pattern.compile("(?<prefix>(Prof|Dr)\\.)(.*)");
    public final Pattern STREET_LINE = Pattern.compile("(.*)(\\d{5})(.*)");
    public final Pattern CITY_LINE = Pattern.compile("^[0-9]$[a-zA-Z]");
    public final Pattern PHONE_LINE = Pattern.compile("^[0-9\\-]");
    public final Pattern FAX_LINE = Pattern.compile("Fax:\\.");
}
