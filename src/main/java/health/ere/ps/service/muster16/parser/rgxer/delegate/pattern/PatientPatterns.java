package health.ere.ps.service.muster16.parser.rgxer.delegate.pattern;

import java.util.regex.Pattern;

public class PatientPatterns extends Patterns {
    //Check PractitionerPatterns
    public final Pattern NAME_PREFIX = Pattern.compile("(Prof|Dr)\\.");
    public final Pattern FIRST_NAME_LINE = Pattern.compile("(?<prefix>(Prof|Dr)\\.)(.*)");
    public final Pattern ADDRESS_LINE = Pattern.compile("(.*)(\\d{5})(.*)");
    public final Pattern STREET_LINE = Pattern.compile("(\\D+)(\\d+( ?[a-zA-Z])?)");
}
