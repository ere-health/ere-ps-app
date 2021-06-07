package health.ere.ps.model.dgc;

import java.util.Objects;

public class Nam {
    public String fn;
    public String gn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nam nam = (Nam) o;
        return Objects.equals(fn, nam.fn) && Objects.equals(gn, nam.gn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fn, gn);
    }
}
