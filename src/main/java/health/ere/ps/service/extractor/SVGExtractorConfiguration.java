package health.ere.ps.service.extractor;


public class SVGExtractorConfiguration {
    String NAME;
    float X_OFFSET;
    float Y_OFFSET;
    float SCALE;
    int ROTATE_DEGREE;
    String MUSTER_16_TEMPLATE;

    public static final SVGExtractorConfiguration CGM_TURBO_MED = new SVGExtractorConfiguration("CGM_TURBO_MED", -8f, -12f, 0.8f, 0);
    public static final SVGExtractorConfiguration CGM_Z1 = new SVGExtractorConfiguration("CGM_Z1", 370f, 150f, 0.75f, 90);
    public static final SVGExtractorConfiguration APRAXOS = new SVGExtractorConfiguration("APRAXOS", -10f, 0f, 0.75f, 0);
    public static final SVGExtractorConfiguration DENS = new SVGExtractorConfiguration("DENS", -15f, -0f, 0.75f, 0,"/svg-extract-templates/Muster-16-Template_dens.svg"); 

    public SVGExtractorConfiguration(String NAME, float X_OFFSET, float Y_OFFSET, float SCALE, int ROTATE_DEGREE) {
        this.NAME = NAME;
        this.X_OFFSET = X_OFFSET;
        this.Y_OFFSET = Y_OFFSET;
        this.SCALE = SCALE;
        this.ROTATE_DEGREE = ROTATE_DEGREE;
    }
    
    public SVGExtractorConfiguration(String NAME, float X_OFFSET, float Y_OFFSET, float SCALE, int ROTATE_DEGREE, String MUSTER_16_TEMPLATE) {
        this(NAME, X_OFFSET,Y_OFFSET, SCALE, ROTATE_DEGREE);
        this.MUSTER_16_TEMPLATE = MUSTER_16_TEMPLATE;
    }
}
