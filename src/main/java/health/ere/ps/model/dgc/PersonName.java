package health.ere.ps.model.dgc;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
}
