package health.ere.ps.model.muster16;


public final class MedicationString {
    private final String name;
    private final String size;
    private final String form;
    private final String dosage;
    private final String instructions;
    private final String pzn;

    public MedicationString(String name, String size, String form, String dosage, String instructions, String pzn) {
        this.name = name;
        this.size = size;
        this.form = form;
        this.dosage = dosage;
        this.instructions = instructions;
        this.pzn = pzn;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getForm() {
        return form;
    }

    public String getDosage() {
        return dosage;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getPzn() {
        return pzn;
    }
}
