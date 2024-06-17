package health.ere.ps.jmx;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

@SuppressWarnings("unused") //used by jmx
public interface StatusMXBean {
    CompositeData getStatus() throws OpenDataException;
}
