package health.ere.ps.service.muster16.parser;

import java.util.regex.Pattern;

public class RegexPatterns {

    static final Pattern EXTRA_WHITE_SPACE = Pattern.compile("\\s+");
    static final Pattern NUMBERS = Pattern.compile("(\\d+)", Pattern.DOTALL);
    static final Pattern ADDRESS_LINE = Pattern.compile("(.*)(\\d{5})(.*)");
    static final Pattern STREET_LINE = Pattern.compile("(\\D+)(\\d+)");
    static final Pattern DATE = Pattern.compile("\\d+[.-/]\\d+[.-/]\\d+");
}
