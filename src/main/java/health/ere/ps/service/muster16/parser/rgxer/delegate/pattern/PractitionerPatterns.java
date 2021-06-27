package health.ere.ps.service.muster16.parser.rgxer.delegate.pattern;

import java.util.regex.Pattern;

public class PractitionerPatterns extends Patterns {
    public final Pattern NAME_PREFIX = Pattern.compile("((Dr|dr|Med|med|Prof|prof)\\.)+");
    public final Pattern NAME_LINE = Pattern.compile(
            "((Dr|dr|Med|med|Prof|prof)\\.)*([a-z A-ZäöüÄÖÜß]+)(-){0,1}([a-z A-ZäöüÄÖÜß]+)");
    public final Pattern STREET_LINE = Pattern.compile("^[a-z A-ZäöüÄÖÜß]+(\\.{1})?.*[0-9]+");
    public final Pattern CITY_LINE = Pattern.compile(".*\\d{5}.*");
    public final Pattern PHONE_LINE = Pattern.compile(".*[0-9 \\-/]{10,}+");
    public final Pattern FAX_LINE = Pattern.compile("^(Fax:).*");
}
