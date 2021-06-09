package health.ere.ps.model.dgc;

import java.util.List;
import java.util.Objects;

public class VaccinationCertificateRequest extends CertificateRequestBase {
    public List<V> v;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VaccinationCertificateRequest that = (VaccinationCertificateRequest) o;
        return Objects.equals(v, that.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), v);
    }
}
