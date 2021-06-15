package health.ere.ps.service.muster16.parser.rgxer.model;

import java.util.Objects;

public class MedicationRecord {

    private final String PZN;
    private final String name;
    private final String norm;
    private final String amount;
    private final String form;

    public MedicationRecord(String PZN, String name, String norm, String amount, String form) {
        this.PZN = PZN;
        this.name = name;
        this.norm = norm;
        this.amount = amount;
        this.form = form;
    }

    public String getPZN() {
        return PZN;
    }

    public String getName() {
        return name;
    }

    public String getNorm() {
        return norm;
    }

    public String getAmount() {
        return amount;
    }

    public String getForm() {
        return form;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicationRecord dataEntry = (MedicationRecord) o;
        return Objects.equals(PZN, dataEntry.PZN) && Objects.equals(name, dataEntry.name) && Objects.equals(norm, dataEntry.norm) && Objects.equals(amount, dataEntry.amount) && Objects.equals(form, dataEntry.form);
    }

    @Override
    public int hashCode() {
        return PZN.hashCode();
    }
}
