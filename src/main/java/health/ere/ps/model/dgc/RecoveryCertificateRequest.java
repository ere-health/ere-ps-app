package health.ere.ps.model.dgc;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RecoveryCertificateRequest
 */
public class RecoveryCertificateRequest extends CertificateRequestBase {
    @JsonProperty("r")
    private List<RecoveryEntry> r = new ArrayList<RecoveryEntry>();

    public RecoveryCertificateRequest addRItem(RecoveryEntry rItem) {
        this.r.add(rItem);
        return this;
    }

    /**
     * Recovery Certificate Entry
     *
     * @return r
     **/
    @JsonProperty("r")
    @NotNull
    @Valid
    @Size(min = 1, max = 1)
    public List<RecoveryEntry> getR() {
        return r;
    }

    public void setR(List<RecoveryEntry> r) {
        this.r = r;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecoveryCertificateRequest recoveryCertificateRequest = (RecoveryCertificateRequest) o;
        return Objects.equals(this.r, recoveryCertificateRequest.r) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, super.hashCode());
    }


    @Override
    public String toString() {
        return "class RecoveryCertificateRequest {\n" +
                "    " + super.toString() + "\n" +
                "    r: " + r + "\n" +
                "}";
    }
}
