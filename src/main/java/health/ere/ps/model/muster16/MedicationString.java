package health.ere.ps.model.muster16;


public class MedicationString {
    private final String name;
    private final String size;
    private final String dosageInstruction;
    private final String pzn;

    public MedicationString(String name, String size, String dosageInstruction, String pzn) {
        this.name = name;
        this.size = size;
        this.dosageInstruction = dosageInstruction;
        this.pzn = pzn;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getDosageInstruction() {
        return dosageInstruction;
    }

    public String getPzn() {
        return pzn;
    }
}
