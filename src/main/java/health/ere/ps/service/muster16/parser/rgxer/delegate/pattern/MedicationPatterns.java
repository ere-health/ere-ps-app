package health.ere.ps.service.muster16.parser.rgxer.delegate.pattern;

import java.util.regex.Pattern;

public class MedicationPatterns extends Patterns {

    public final Pattern DOSAGE_PAT = Pattern.compile("[01]\\s*-\\s*[01]\\s*-\\s*[01]");
    public final Pattern STRENGTH_PAT = Pattern.compile("(?<value>\\d+([.,]\\d+)?)\\s*(?<unit>(μg|mg|g|ml|%))");
    public final Pattern SEPARATOR_PAT = Pattern.compile("[^\\w\\d]+");

    // https://applications.kbv.de/xml/S_KBV_NORMGROESSE_V1.00.xml
    public final Pattern SIZE_PAT = Pattern.compile("\\b(KA|KTP|N[1-3B]|KP)\\b");
    public final Pattern ZCODE_PAT = Pattern.compile("[01]\\s*-\\s*[01]\\s*-\\s*[01]");
    public final Pattern PZN_PAT = Pattern.compile("(PZN)?\\s*:?\\s*(?<value>\\d{8})");
}
