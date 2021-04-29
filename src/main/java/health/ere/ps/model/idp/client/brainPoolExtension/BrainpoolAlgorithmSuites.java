package health.ere.ps.model.idp.client.brainPoolExtension;
import org.jose4j.jws.EcdsaUsingShaAlgorithm;
import org.jose4j.jws.JsonWebSignatureAlgorithm;

public class BrainpoolAlgorithmSuites extends EcdsaUsingShaAlgorithm implements
        JsonWebSignatureAlgorithm {

    public BrainpoolAlgorithmSuites(final String id, final String javaAlgo,
                                    final String curveName,
        final int signatureByteLength) {
        super(id, javaAlgo, curveName, signatureByteLength);
    }

    public static class EcdsaBP256R1UsingSha256 extends BrainpoolAlgorithmSuites {
        public EcdsaBP256R1UsingSha256() {
            super(BrainpoolAlgorithmSuiteIdentifiers.INTERNAL_BRAINPOOL256_USING_SHA256,
                    "SHA256withECDSA", BrainpoolCurves.BP_256, 64);
        }
    }

    public static class EcdsaBP384R1UsingSha384 extends BrainpoolAlgorithmSuites {
        public EcdsaBP384R1UsingSha384() {
            super(BrainpoolAlgorithmSuiteIdentifiers.INTERNAL_BRAINPOOL384_USING_SHA384,
                    "SHA384withECDSA", BrainpoolCurves.BP_384, 96);
        }
    }

    public static class EcdsaBP512R1UsingSha512 extends BrainpoolAlgorithmSuites {
        public EcdsaBP512R1UsingSha512() {
            super(BrainpoolAlgorithmSuiteIdentifiers.INTERNAL_BRAINPOOL512_USING_SHA512,
                    "SHA512withECDSA", BrainpoolCurves.BP_512, 132);
        }
    }

}
