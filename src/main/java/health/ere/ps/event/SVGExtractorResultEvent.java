package health.ere.ps.event;

import java.util.Map;

public final class SVGExtractorResultEvent {
    private final Map<String,String> svgExtractionResult;

    public SVGExtractorResultEvent(Map<String,String> svgExtractionResult) {
        this.svgExtractionResult = svgExtractionResult;
    }

    public Map<String, String> getSvgExtractionResult() {
        return svgExtractionResult;
    }
}
