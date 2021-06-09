package health.ere.ps.model.dgc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Single recovery data entry.
 */
public class RecoveryEntry {
    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tg")
    private String tg = null;

    @JsonProperty("fr")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fr = null;

    @JsonProperty("is")
    private String is = null;

    @JsonProperty("df")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate df = null;

    @JsonProperty("du")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate du = null;

    /**
     * Identifier of the health professional location (i.e. BSNR or similar identifer). It will be used in the construction of the DGCI (digitial green certificate identifier). Due to the specification of the DGCI only the use of uppercase letters and numbers 0-9 are allowed.
     *
     * @return id
     **/
    @JsonProperty("id")
    @NotNull
    @Pattern(regexp = "^[0-9A-Z]+$")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get tg
     *
     * @return tg
     **/
    @JsonProperty("tg")
    @NotNull
    @Valid
    public String getTg() {
        return tg;
    }

    public void setTg(String tg) {
        this.tg = tg;
    }

    /**
     * First positive test result date as ISO 8601.
     *
     * @return fr
     **/
    @JsonProperty("fr")
    @NotNull
    @Valid
    public LocalDate getFr() {
        return fr;
    }

    public void setFr(LocalDate fr) {
        this.fr = fr;
    }

    /**
     * Get is
     *
     * @return is
     **/
    @JsonProperty("is")
    @NotNull
    @Valid
    public String getIs() {
        return is;
    }

    public void setIs(String is) {
        this.is = is;
    }

    /**
     * Certificate valid from date as ISO 8601.
     *
     * @return df
     **/
    @JsonProperty("df")
    @NotNull
    @Valid
    public LocalDate getDf() {
        return df;
    }

    public void setDf(LocalDate df) {
        this.df = df;
    }

    /**
     * Certificate valid until date as ISO 8601.
     *
     * @return du
     **/
    @JsonProperty("du")
    @NotNull
    @Valid
    public LocalDate getDu() {
        return du;
    }

    public void setDu(LocalDate du) {
        this.du = du;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecoveryEntry recoveryEntry = (RecoveryEntry) o;
        return Objects.equals(this.id, recoveryEntry.id) &&
                Objects.equals(this.tg, recoveryEntry.tg) &&
                Objects.equals(this.fr, recoveryEntry.fr) &&
                Objects.equals(this.is, recoveryEntry.is) &&
                Objects.equals(this.df, recoveryEntry.df) &&
                Objects.equals(this.du, recoveryEntry.du);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tg, fr, is, df, du);
    }


    @Override
    public String toString() {

        return "class RecoveryEntry {\n" +
                "    id: " + id + "\n" +
                "    tg: " + tg + "\n" +
                "    fr: " + fr + "\n" +
                "    is: " + is + "\n" +
                "    df: " + df + "\n" +
                "    du: " + du + "\n" +
                "}";
    }
}
