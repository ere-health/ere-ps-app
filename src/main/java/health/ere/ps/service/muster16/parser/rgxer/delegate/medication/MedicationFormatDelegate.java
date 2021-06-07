package health.ere.ps.service.muster16.parser.rgxer.delegate.medication;

import health.ere.ps.service.muster16.parser.rgxer.formatter.FormattingChain;

import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MedicationFormatDelegate {


    private final Pattern EXTRA_WHITE_SPACE = Pattern.compile("\\s+");


    private String removeExtraSpaces(String entry) {
        return EXTRA_WHITE_SPACE.matcher(entry).replaceAll(" ").trim();
    }

    private String cleanToken(String entry) {
        return removeExtraSpaces(entry);
    }

    private String cleanNoise(String entry, Pattern pattern) {
        Matcher matcher = pattern.matcher(entry);
        return matcher.find() ? matcher.group(0) : cleanToken(entry);
    }

    String formatName(String entry) {
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }

    String formatDosage(String entry){
        return FormattingChain.format(entry).apply(this::cleanToken).get();
    }
}
