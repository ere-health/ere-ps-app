package health.ere.ps.model.dgc;

import java.util.Objects;

public class V {
    public String id;
    public String tg;
    public String vp;
    public String mp;
    public String ma;
    public Integer dn;
    public Integer sd;
    public String dt;
    public String co;
    public String is;
    public String ci;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V v = (V) o;
        return Objects.equals(id, v.id) && Objects.equals(tg, v.tg) && Objects.equals(vp, v.vp) && Objects.equals(mp, v.mp) && Objects.equals(ma, v.ma) && Objects.equals(dn, v.dn) && Objects.equals(sd, v.sd) && Objects.equals(dt, v.dt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tg, vp, mp, ma, dn, sd, dt);
    }
}
