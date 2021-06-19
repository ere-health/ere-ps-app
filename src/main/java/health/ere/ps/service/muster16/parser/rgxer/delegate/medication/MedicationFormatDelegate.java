package health.ere.ps.service.muster16.parser.rgxer.delegate.medication;

import health.ere.ps.service.muster16.parser.rgxer.formatter.FormattingChain;
import health.ere.ps.service.muster16.parser.rgxer.delegate.pattern.MedicationPatterns;

public class MedicationFormatDelegate {


    private final MedicationPatterns patterns;

    public MedicationFormatDelegate() {
        patterns = new MedicationPatterns();
    }

    private String removeExtraSpaces(String entry) {
        return patterns.EXTRA_WHITE_SPACE.matcher(entry).replaceAll(" ").trim();
    }

    private String cleanToken(String entry) {
        return removeExtraSpaces(entry);
    }

    private String removePZN(String entry) {
        return patterns.PZN_PAT.matcher(entry).replaceAll(" ");
    }

    String formatName(String entry) {
        return FormattingChain.format(entry)
                .apply(this::removePZN)
                .apply(this::cleanToken).get();
    }

    String formatDosage(String entry) {
        return FormattingChain.format(entry)
                .apply(this::removePZN)
                .apply(this::cleanToken).get();
    }
}
