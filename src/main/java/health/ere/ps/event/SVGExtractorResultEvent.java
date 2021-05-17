package health.ere.ps.event;

import java.util.Map;

public class SVGExtractorResultEvent {
    public Map<String,String> map;
    public SVGExtractorResultEvent(Map<String,String> map) {
        this.map = map;
    }
}
