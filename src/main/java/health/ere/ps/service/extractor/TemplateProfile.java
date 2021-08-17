package health.ere.ps.service.extractor;

public enum TemplateProfile {

    CGM_TURBO_MED(new SVGExtractorConfiguration("CGM_TURBO_MED", -8f, -12f, 0.8f, 0)),
    CGM_Z1(new SVGExtractorConfiguration("CGM_Z1", 370f, 150f, 0.75f, 90)),
    APRAXOS(new SVGExtractorConfiguration("APRAXOS", -10f, 0f, 0.75f, 0)),
    DENS(new SVGExtractorConfiguration("DENS", -15f, -0f, 0.75f, 0, "/svg-extract-templates/Muster-16-Template_dens.svg")),
    DENS_LANDSCAPE(new SVGExtractorConfiguration("DENS_LANDSCAPE", 400f, 150f, 0.75f, 0, "/svg-extract-templates/Muster-16-Template_dens_2.svg"));

    public final SVGExtractorConfiguration configuration;

    TemplateProfile(SVGExtractorConfiguration configuration) {
        this.configuration = configuration;
    }
}