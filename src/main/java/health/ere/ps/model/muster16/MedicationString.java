package health.ere.ps.model.muster16;


public class MedicationString {
    private String name;
    private String size;
    private String form;
    private String dosage;
    private String instructions;
    private String pzn;

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

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getPzn() {
        return pzn;
    }

    public void setPzn(String pzn) {
        this.pzn = pzn;
    }
}
