package health.ere.ps.model.idp.client.brainPoolExtension;

public class BrainpoolAlgorithmSuiteIdentifiers {
    public static final String INTERNAL_BRAINPOOL256_USING_SHA256 = "BP256R1";
    public static final String INTERNAL_BRAINPOOL384_USING_SHA384 = "BP384R1";
    public static final String INTERNAL_BRAINPOOL512_USING_SHA512 = "BP512R1";
    public static final String BRAINPOOL256_USING_SHA256 = getValueAndExecuteInitialisation(
        INTERNAL_BRAINPOOL256_USING_SHA256);
    public static final String BRAINPOOL384_USING_SHA384 = getValueAndExecuteInitialisation(
        INTERNAL_BRAINPOOL384_USING_SHA384);
    public static final String BRAINPOOL512_USING_SHA512 = getValueAndExecuteInitialisation(
        INTERNAL_BRAINPOOL512_USING_SHA512);

    private BrainpoolAlgorithmSuiteIdentifiers() {

    }

    private static String getValueAndExecuteInitialisation(final String value) {
        BrainpoolCurves.init();
        return value;
    }
}
