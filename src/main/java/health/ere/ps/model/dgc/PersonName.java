package health.ere.ps.model.dgc;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Person name
 * Name of the person which receives the certificate.
 */
public class PersonName {

    /**
     * Family name
     * The family or primary name(s) of the person addressed in the certificate
     * example: "Musterfrau‐Dießner"
     */
    @Size(max = 50)
    @NotNull
    public String fn;
    /**
     * Given name
     * The given name(s) of the person addressed in the certificate
     * example: "Erika Dörte"
     */
    @Size(max = 50)
    public String gn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonName that = (PersonName) o;
        return Objects.equals(fn, that.fn) && Objects.equals(gn, that.gn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fn, gn);
    }
}
