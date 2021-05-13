package health.ere.ps.service.extractor;

public class SVGExtractorConfiguration {
    String NAME;
    float X_OFFSET;
    float Y_OFFSET;
    float SCALE;
    int ROTATE_DEGREE;

    public static final SVGExtractorConfiguration CGM_TURBO_MED = new SVGExtractorConfiguration("CGM_TURBO_MED", -4f, -12f, 1f, 0);
    public static final SVGExtractorConfiguration CGM_Z1 = new SVGExtractorConfiguration("CGM_Z1", 370f, 150f, 0.75f, 90);
    public static final SVGExtractorConfiguration DENS = new SVGExtractorConfiguration("CGM_Z1", 370f, 150f, 0.75f, 90);

    public SVGExtractorConfiguration(String NAME, float X_OFFSET, float Y_OFFSET, float SCALE, int ROTATE_DEGREE) {
        this.NAME = NAME;
        this.X_OFFSET = X_OFFSET;
        this.Y_OFFSET = Y_OFFSET;
        this.SCALE = SCALE;
        this.ROTATE_DEGREE = ROTATE_DEGREE;
    }
}
