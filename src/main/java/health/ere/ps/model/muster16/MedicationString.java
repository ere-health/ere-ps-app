package health.ere.ps.model.muster16;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MedicationString {
    public String name;
    public String size;
    public String dosageInstruction;
    public String pzn;

    private static final Pattern MEDICATION_LINE = Pattern.compile("(.*)(N\\d)(.*)(PZN ?)(\\d+)");

    public MedicationString() {

    }

    public MedicationString(String name) {
        this.name = name;
        Matcher m = MEDICATION_LINE.matcher(name);
        if(m.matches()) {
            this.name = m.group(1);
            this.size = m.group(2);
            this.dosageInstruction = m.group(3);
            this.pzn = m.group(5);
        }
    }
}
