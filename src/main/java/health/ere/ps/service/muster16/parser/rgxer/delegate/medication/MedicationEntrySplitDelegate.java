package health.ere.ps.service.muster16.parser.rgxer.delegate.medication;

import health.ere.ps.service.muster16.parser.rgxer.delegate.pattern.MedicationPatterns;

import java.util.regex.Matcher;


public class MedicationEntrySplitDelegate {

    private final MedicationPatterns patterns;

    public MedicationEntrySplitDelegate() {
        this.patterns = new MedicationPatterns();
    }

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
        final Matcher matcher = patterns.ZCODE_PAT.matcher(entry);
        return matcher.find() ? matcher.start() : -1;
    }
    //endregion

    //region Method 2: matching common endings of medication name
    private int strengthEndIndex(String entry) {
        final Matcher matcher = patterns.STRENGTH_PAT.matcher(entry);
        return matcher.find() ? matcher.end() : -1;
    }

    private int sizeEndIndex(String entry) {
        final Matcher matcher = patterns.SIZE_PAT.matcher(entry);
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
