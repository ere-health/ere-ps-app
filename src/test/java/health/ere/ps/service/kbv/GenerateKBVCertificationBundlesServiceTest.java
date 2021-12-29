package health.ere.ps.service.kbv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.parser.XMLParserException;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.profile.TitusTestProfile;
import health.ere.ps.service.gematik.ERezeptWorkflowService;
import health.ere.ps.service.pdf.DocumentService;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@Disabled
@TestProfile(TitusTestProfile.class)
public class GenerateKBVCertificationBundlesServiceTest {

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    @Inject
    ERezeptWorkflowService eRezeptWorkflowService;

    @Inject
    DocumentService documentService;

    @Inject
    GenerateKBVCertificationBundlesService service;

    IParser iParser = FhirContext.forR4().newXmlParser().setPrettyPrint(true);

    static boolean useTitus = true;

    static boolean generateSignatureAndPdf = true;
    static boolean validateResources = false;

    @Test
    public void testPF01() throws IOException, InvalidCanonicalizerException, XMLParserException, CanonicalizationException, ERezeptWorkflowException, FOPException, TransformerException {
        Bundle bundle = service.PF01();
        boolean generateSignature = true;
        boolean generatePdf = true;
        processBundle("PF01", generateSignature, generatePdf, bundle);

    }

    
    private void processBundle(String testCase, boolean generateSignature, boolean generatePdf, Bundle bundle)
            throws InvalidCanonicalizerException, XMLParserException, IOException, CanonicalizationException,
            ERezeptWorkflowException, FOPException, TransformerException {
        byte[] canonicalBytes = ERezeptWorkflowService.getCanonicalXmlBytes(bundle);
        Files.write(Paths.get("src/test/resources/kbv-zip/"+testCase+".xml"), canonicalBytes);
        if(generateSignatureAndPdf){
            if(validateResources) {
                prescriptionBundleValidator.validateResource(bundle, true);
            }
            if(generateSignature) {
                SignResponse signResponse = eRezeptWorkflowService.signBundleWithIdentifiers(bundle);
                Files.write(Paths.get("src/test/resources/kbv-zip/"+testCase+".p7s"), signResponse.getSignatureObject().getBase64Signature().getValue());
            }
            if(generatePdf) {
                List<Bundle> list = Arrays.asList(bundle);
                createPdf(testCase, list);
            }
        }
    }


    private void createPdf(String testCase, List<Bundle> list) throws IOException, FOPException, TransformerException {
        if(generateSignatureAndPdf){
            List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables = new ArrayList<>();
            if(useTitus) {
                eRezeptWorkflowService.requestNewAccessTokenIfNecessary();
                bundleWithAccessCodeOrThrowables = eRezeptWorkflowService.createMultipleERezeptsOnPrescriptionServer(list);
            } else {
                bundleWithAccessCodeOrThrowables = list.stream().map(bundle -> new BundleWithAccessCodeOrThrowable(bundle, "8279c66a752f64608387273209975457d806d0f66eeb8424f2e696de75b9acf5")).collect(Collectors.toList());
            }   

            ByteArrayOutputStream byteArrayOutputStream = documentService.generateERezeptPdf(bundleWithAccessCodeOrThrowables);

            Files.write(Paths.get("src/test/resources/kbv-zip/Dokumentation/"+testCase+".pdf"), byteArrayOutputStream.toByteArray());
        }
    }

    @Test
    public void testPF02() throws IOException, InvalidCanonicalizerException, XMLParserException, CanonicalizationException, ERezeptWorkflowException, FOPException, TransformerException {
        Bundle bundle = service.PF02();
        
        boolean generateSignature = false;
        boolean generatePdf = true;
        processBundle("PF02", generateSignature, generatePdf, bundle);

    }

    @Test
    public void testPF03_PF04_PF05_PF06() throws IOException, InvalidCanonicalizerException, XMLParserException, CanonicalizationException, ERezeptWorkflowException, FOPException, TransformerException {
        List<Bundle> list = new ArrayList<>();
        Bundle bundle = service.PF03();
          
        boolean generateSignature = true;
        boolean generatePdf = false;
        processBundle("PF03", generateSignature, generatePdf, bundle);
        list.add(bundle);
   
        bundle = service.PF04();
        
        generateSignature = true;
        generatePdf = false;
        processBundle("PF04", generateSignature, generatePdf, bundle);
        list.add(bundle);
   
        bundle = service.PF05();
        
        generateSignature = true;
        generatePdf = false;
        processBundle("PF05", generateSignature, generatePdf, bundle);
        list.add(bundle);

        createPdf("PF06", list);
    }

    @Test
    public void testPF07() throws IOException, InvalidCanonicalizerException, XMLParserException, CanonicalizationException, ERezeptWorkflowException, FOPException, TransformerException {
        Bundle bundle = service.PF07();
        
        boolean generateSignature = false;
        boolean generatePdf = false;
        processBundle("PF07", generateSignature, generatePdf, bundle);
    }

    @Test
    public void testPF08() throws IOException, FOPException, TransformerException {
        GenerateKBVCertificationBundlesService service = new GenerateKBVCertificationBundlesService();
        List<Bundle> bundles = service.PF08();
        
        for(int i = 1;i<=3;i++) {
            Bundle bundle = bundles.get(i-1);
            String bundleString = iParser.encodeResourceToString(bundle);
            Files.write(Paths.get("src/test/resources/kbv-zip/PF08_"+i+".xml"), bundleString.getBytes());
            // prescriptionBundleValidator.validateResource(bundle, true);
        }

        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables = new ArrayList<>();
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundles.get(0), "c573b8da4a6ce5d3fe15adda16f9474ad2a25746e892f959fada4477019eebe5"));
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundles.get(1), "bbeaae841cb813f6a8c4b9ed0a49eb91ee6b0fc83adea1f60d8cd3db8250b60e"));
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundles.get(2), "f89cdbdc332eaea5bbd0ab9a2d801e579fec0ce1a2856793110bd0be8c214dfe"));
        ByteArrayOutputStream byteArrayOutputStream = documentService.generateERezeptPdf(bundleWithAccessCodeOrThrowables);

        Files.write(Paths.get("src/test/resources/kbv-zip/Dokumentation/PF08.pdf"), byteArrayOutputStream.toByteArray());
    }

    @Test
    public void testPF09() throws IOException, InvalidCanonicalizerException, XMLParserException, CanonicalizationException, ERezeptWorkflowException, FOPException, TransformerException {
        Bundle bundle = service.PF09();
        
        boolean generateSignature = false;
        boolean generatePdf = false;
        processBundle("PF09", generateSignature, generatePdf, bundle);
    }

    @Test
    public void testPF10() throws IOException, InvalidCanonicalizerException, XMLParserException, CanonicalizationException, ERezeptWorkflowException, FOPException, TransformerException {
        Bundle bundle = service.PF10();
        
        boolean generateSignature = false;
        boolean generatePdf = true;
        processBundle("PF10", generateSignature, generatePdf, bundle);
    }

    @Test
    @Disabled
    public void testRegeneratePdf() throws IOException, FOPException, TransformerException {

        genPDF("PF01", "d78fe79c81be9541bcf7a95c8254821e3ab3e88eaa1898db9e1b78a982fc94b2");
        genPDF("PF02", "14e5ed98461cbe0e2aa647c45b03fbbb11340dd55e8324563319a89ff93a5f30");
        
        List<Bundle> bundles = new ArrayList<>(); 
        
        bundles.add(iParser.parseResource(Bundle.class, getXmlString("src/test/resources/kbv-zip/PF03.xml")));
        bundles.add(iParser.parseResource(Bundle.class, getXmlString("src/test/resources/kbv-zip/PF04.xml")));
        bundles.add(iParser.parseResource(Bundle.class, getXmlString("src/test/resources/kbv-zip/PF05.xml")));
        

        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables = new ArrayList<>();
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundles.get(0), "9c92aaa9317950ca60149a909c502382d88067bc97aaaafeccf6d33b10b61d7f"));
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundles.get(1), "83275b6c9512456ad8197bc4d5e8a53092dc30471ad11abf163e9fa7ae84fe42"));
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundles.get(2), "c9784d153fbc8cecd51e05fba60535541affe6bf5f454e7081de5cbd6aee0d99"));
        ByteArrayOutputStream byteArrayOutputStream = documentService.generateERezeptPdf(bundleWithAccessCodeOrThrowables);

        Files.write(Paths.get("src/test/resources/kbv-zip/Dokumentation/PF06.pdf"), byteArrayOutputStream.toByteArray());
        

        bundles = new ArrayList<>(); 
        for(int i = 1;i<=3;i++) {
            bundles.add(iParser.parseResource(Bundle.class, getXmlString("src/test/resources/kbv-zip/PF08_"+i+".xml")));
        }

        bundleWithAccessCodeOrThrowables = new ArrayList<>();
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundles.get(0), "c573b8da4a6ce5d3fe15adda16f9474ad2a25746e892f959fada4477019eebe5"));
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundles.get(1), "bbeaae841cb813f6a8c4b9ed0a49eb91ee6b0fc83adea1f60d8cd3db8250b60e"));
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundles.get(2), "f89cdbdc332eaea5bbd0ab9a2d801e579fec0ce1a2856793110bd0be8c214dfe"));
        byteArrayOutputStream = documentService.generateERezeptPdf(bundleWithAccessCodeOrThrowables);

        Files.write(Paths.get("src/test/resources/kbv-zip/Dokumentation/PF08.pdf"), byteArrayOutputStream.toByteArray());
    }

    @Test
    public void testRegeneratePdfPF10() throws IOException, FOPException, TransformerException {
        genPDF("PF10", "c509232b3dea6d2303b00ae170a64d2faa8e004246798c154de0fa0e3fc9c9fb");
    }


    private void genPDF(String testCase, String accessCode) throws IOException, FOPException, TransformerException {
        Bundle bundle = iParser.parseResource(Bundle.class, getXmlString("src/test/resources/kbv-zip/"+testCase+".xml"));
        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables = new ArrayList<>();
        bundleWithAccessCodeOrThrowables.add(new BundleWithAccessCodeOrThrowable(bundle, accessCode));
        ByteArrayOutputStream byteArrayOutputStream = documentService.generateERezeptPdf(bundleWithAccessCodeOrThrowables);

        Files.write(Paths.get("src/test/resources/kbv-zip/Dokumentation/"+testCase+".pdf"), byteArrayOutputStream.toByteArray());
    }

    private String getXmlString(String string) throws IOException {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+Files.readString(Paths.get(string));
    }
}
