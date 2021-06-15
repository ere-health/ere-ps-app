package health.ere.ps.service.extractor;


public class SVGExtractorConfiguration {

    public final String NAME;
    public final float X_OFFSET;
    public final float Y_OFFSET;
    public final float SCALE;
    public final int ROTATE_DEGREE;
    public final String MUSTER_16_TEMPLATE;

    public SVGExtractorConfiguration(String NAME, float X_OFFSET, float Y_OFFSET, float SCALE, int ROTATE_DEGREE) {
        this(NAME, X_OFFSET, Y_OFFSET, SCALE, ROTATE_DEGREE, null);
    }

    public SVGExtractorConfiguration(String NAME, float X_OFFSET, float Y_OFFSET, float SCALE, int ROTATE_DEGREE, String MUSTER_16_TEMPLATE) {
        this.NAME = NAME;
        this.X_OFFSET = X_OFFSET;
        this.Y_OFFSET = Y_OFFSET;
        this.SCALE = SCALE;
        this.ROTATE_DEGREE = ROTATE_DEGREE;
        this.MUSTER_16_TEMPLATE = MUSTER_16_TEMPLATE;
    }
}
