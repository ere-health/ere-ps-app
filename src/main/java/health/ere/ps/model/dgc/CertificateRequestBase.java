package health.ere.ps.model.dgc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Common Digital Green Certificate (DGC) request data containing the name and date of birth of the person receiving the certificate.
 */
public class CertificateRequestBase implements CertificateRequest {

    /**
     * Name of the person which receives the certificate.
     */
    @JsonProperty("nam")
    private PersonName nam = null;

    /**
     * Date of birth.
     * Date of Birth of the person addressed in the DGC. ISO 8601 date format restricted to range 1900-2099
     */
    @JsonProperty("dob")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dob = null;

    /**
     * Name of the person which receives the certificate.
     *
     * @return nam
     **/
    @JsonProperty("nam")
    @NotNull
    public PersonName getNam() {
        return nam;
    }

    public void setNam(PersonName nam) {
        this.nam = nam;
    }

    /**
     * Date of Birth of the person addressed in the DGC. ISO 8601 date format restricted to range 1900-2099
     *
     * @return dob
     **/
    @JsonProperty("dob")
    @NotNull
    @Valid
    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CertificateRequestBase certificateRequestBase = (CertificateRequestBase) o;
        return Objects.equals(this.nam, certificateRequestBase.nam) &&
                Objects.equals(this.dob, certificateRequestBase.dob);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nam, dob);
    }


    @Override
    public String toString() {

        return "class CertificateRequestBase {\n" +
                "    nam: " + nam + "\n" +
                "    dob: " + dob + "\n" +
                "}";
    }
}
