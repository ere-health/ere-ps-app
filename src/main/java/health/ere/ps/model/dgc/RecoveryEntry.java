package health.ere.ps.model.dgc;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
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
    private Date fr = null;

    @JsonProperty("is")
    private String is = null;

    @JsonProperty("df")
    private Date df = null;

    @JsonProperty("du")
    private Date du = null;

    public RecoveryEntry id(String id) {
        this.id = id;
        return this;
    }

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

    public RecoveryEntry tg(String tg) {
        this.tg = tg;
        return this;
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

    public RecoveryEntry fr(Date fr) {
        this.fr = fr;
        return this;
    }

    /**
     * First positive test result date as ISO 8601.
     *
     * @return fr
     **/
    @JsonProperty("fr")
    @NotNull
    @Valid
    public Date getFr() {
        return fr;
    }

    public void setFr(Date fr) {
        this.fr = fr;
    }

    public RecoveryEntry is(String is) {
        this.is = is;
        return this;
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

    public RecoveryEntry df(Date df) {
        this.df = df;
        return this;
    }

    /**
     * Certificate valid from date as ISO 8601.
     *
     * @return df
     **/
    @JsonProperty("df")
    @NotNull
    @Valid
    public Date getDf() {
        return df;
    }

    public void setDf(Date df) {
        this.df = df;
    }

    public RecoveryEntry du(Date du) {
        this.du = du;
        return this;
    }

    /**
     * Certificate valid until date as ISO 8601.
     *
     * @return du
     **/
    @JsonProperty("du")
    @NotNull
    @Valid
    public Date getDu() {
        return du;
    }

    public void setDu(Date du) {
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
                "    id: " + toIndentedString(id) + "\n" +
                "    tg: " + toIndentedString(tg) + "\n" +
                "    fr: " + toIndentedString(fr) + "\n" +
                "    is: " + toIndentedString(is) + "\n" +
                "    df: " + toIndentedString(df) + "\n" +
                "    du: " + toIndentedString(du) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
