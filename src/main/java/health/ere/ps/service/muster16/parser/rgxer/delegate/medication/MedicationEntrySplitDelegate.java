package health.ere.ps.service.muster16.parser.rgxer.delegate.medication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MedicationEntrySplitDelegate {

    private final Pattern STRENGTH_PAT = Pattern.compile("(?<value>\\d+([.,]\\d+)?)\\s*(?<unit>(μg|mg|g|ml|%))");
    private final Pattern SIZE_PAT = Pattern.compile("\\b(N[1-3]|KP)\\b");
    private final Pattern ZCODE_PAT = Pattern.compile("[01]\\s*-\\s*[01]\\s*-\\s*[01]");


    int getSplitIndex(String entry) {
        int idx;
        if ((idx = zahlencodeIndex(entry)) != -1)
            return idx;
        else if ((idx = nameEndIndex(entry)) != -1)
            return idx;
        else
            return entry.length();
    }

    //region Method 1: matching with the Zahlencode
    private int zahlencodeIndex(String entry) {
        final Matcher matcher = ZCODE_PAT.matcher(entry);
        return matcher.find() ? matcher.start() : -1;
    }
    //endregion

    //region Method 2: matching common endings of medication name
    private int strengthEndIndex(String entry) {
        final Matcher matcher = STRENGTH_PAT.matcher(entry);
        return matcher.find() ? matcher.end() : -1;
    }

    private int sizeEndIndex(String entry) {
        final Matcher matcher = SIZE_PAT.matcher(entry);
        return matcher.find() ? matcher.end() : -1;
    }

    private int nameEndIndex(String entry) {
        int idx1 = strengthEndIndex(entry), idx2 = sizeEndIndex(entry);
        return Integer.max(idx1, idx2);
    }
    //endregion

    //region Method 3:
    // TODO
    //endregion

}
