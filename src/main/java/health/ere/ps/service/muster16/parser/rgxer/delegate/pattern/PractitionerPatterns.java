package health.ere.ps.service.muster16.parser.rgxer.delegate.pattern;

import java.util.regex.Pattern;

public class PractitionerPatterns extends Patterns {
    public final Pattern NAME_PREFIX = Pattern.compile("(Dr)\\.");
    public final Pattern NAME_LINE = Pattern.compile("(Dr\\.)*([a-z A-Z]+)(-){0,1}([a-z A-Z]+)");
    public final Pattern STREET_LINE = Pattern.compile("^[a-z A-Zß]+(\\.{1})?.*[0-9]+");
    public final Pattern CITY_LINE = Pattern.compile(".*\\d{5}.*");
    public final Pattern PHONE_LINE = Pattern.compile(".*[0-9 \\-/]{10,}+");
    public final Pattern FAX_LINE = Pattern.compile("^(Fax:).*");
}
