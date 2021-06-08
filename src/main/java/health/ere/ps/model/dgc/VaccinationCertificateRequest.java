package health.ere.ps.model.dgc;

import java.util.List;
import java.util.Objects;

public class VaccinationCertificateRequest implements CertificateRequest {
    public PersonName nam;
    public String dob;
    public List<V> v;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VaccinationCertificateRequest that = (VaccinationCertificateRequest) o;
        return Objects.equals(nam, that.nam) && Objects.equals(dob, that.dob) && Objects.equals(v, that.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nam, dob, v);
    }
}
