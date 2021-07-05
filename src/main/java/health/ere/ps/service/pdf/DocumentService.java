package health.ere.ps.service.pdf;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.pdf.ERezeptDocument;
import org.apache.fop.apps.*;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.hl7.fhir.r4.model.Bundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class DocumentService {

    private static final Logger log = Logger.getLogger(DocumentService.class.getName());
    private static final int MAX_NUMBER_OF_MEDICINES_PER_PRESCRIPTIONS = 9;

    private final FhirContext ctx = FhirContext.forR4();
    @Inject
    Event<ERezeptDocumentsEvent> eRezeptDocumentsEvent;
    @Inject
    Event<Exception> exceptionEvent;

    private FopFactory fopFactory;

    @PostConstruct
    public void init() {
        try {
            URI baseURI = getClass().getResource("/fop/").toURI();
            FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(baseURI);
            initConfiguration(fopFactoryBuilder);
            fopFactory = fopFactoryBuilder.build();
        } catch (Exception ex) {
            log.severe("FOP Factory not initializable:" + ex);
            exceptionEvent.fireAsync(ex);
        }
    }

    private void initConfiguration(FopFactoryBuilder fopFactoryBuilder) {
        Configuration cfg;
        try {
            URI uri = getClass().getResource("/fop/fonts/").toURI();
            File physicalFile = new File(uri);
            String absolutePath = physicalFile.getAbsolutePath();
            String config = Files.readString(new File(getClass().getResource("/fop/fop.xconf").toURI()).toPath())
                    .replaceAll("__WILL_BE_REPLACED_IN_DocumentService__", absolutePath);
            cfg = new DefaultConfigurationBuilder().build(new ByteArrayInputStream(config.getBytes()));
            // This is needed to extract the fonts from the war
            // It is unknown yet how quarkus behaves
            // List<String> fonts = Arrays.asList("arial.ttf", "arialbd.ttf", "arialbi.ttf",
            // "ariali.ttf", "ARIALN.TTF",
            // "ARIALNB.TTF", "ARIALNBI.TTF", "ARIALNI.TTF", "ARIALUNI.TTF",
            // "ARIALUNIB.TTF", "ariblk.ttf",
            // "Symbola.ttf");
            // for (String font : fonts) {
            // uri = getClass().getResource("/fop/fonts/" + font).toURI();
            // log.info("Font found: " + uri);
            // }
            // log.log(Level.INFO, "Setting fonts path to: {0}", absolutePath);
            // Configuration fontConfig =
            // cfg.getChildren("renderers")[0].getChildren("renderer")[0]
            // .getChildren("fonts")[0].getChildren("directory")[0];
            // ((DefaultConfiguration)fontConfig)
            // .setValue(absolutePath);

            fopFactoryBuilder.setConfiguration(cfg);
        } catch (IllegalArgumentException | ConfigurationException | ArrayIndexOutOfBoundsException |
                IOException | URISyntaxException e) {
            log.severe("Could not configure FOP from file in classpath: /fonts/fop.xconf:" + e);
            exceptionEvent.fireAsync(e);
        }
    }


    public void onBundlesWithAccessCodes(@ObservesAsync BundlesWithAccessCodeEvent bundlesWithAccessCodeEvent) {
        log.info(String.format("About to create prescription receipts for %d bundles",
        bundlesWithAccessCodeEvent.getBundleWithAccessCodeOrThrowable().size()));
        bundlesWithAccessCodeEvent.getBundleWithAccessCodeOrThrowable().forEach(bundles -> {
            try {
                for (int i = 0; i < bundles.size(); i += MAX_NUMBER_OF_MEDICINES_PER_PRESCRIPTIONS) {
                    log.info(String.format("Processing bundle with %d medication(s)", i));
                    createAndSendPrescriptions(bundles
                            .subList(i, Math.min(i + MAX_NUMBER_OF_MEDICINES_PER_PRESCRIPTIONS, bundles.size())));
                }
            } catch(Exception ex) {
                exceptionEvent.fireAsync(ex);
            }
        });
    }

    private void createAndSendPrescriptions(List<BundleWithAccessCodeOrThrowable> bundles) {
        log.info("Now creating prescription receipts");
        ByteArrayOutputStream boas = generateERezeptPdf(bundles);

        ERezeptDocument eRezeptDocument = new ERezeptDocument(bundles, boas.size() > 0 ? boas.toByteArray() : null);

        log.info("Created prescription receipts");
        eRezeptDocumentsEvent.fireAsync(new ERezeptDocumentsEvent(List.of(eRezeptDocument)));
        log.info("Sending prescription receipts results.");

    }

    public ByteArrayOutputStream generateERezeptPdf(List<BundleWithAccessCodeOrThrowable> bundles) {
        try {
            if (bundles.isEmpty()) {
                log.severe("Cannot generate prescriptions pdf for an empty bundle");
                return new ByteArrayOutputStream();
            }

            File xml = createTemporaryXmlFileFromBundles(bundles);
            return generatePdfInOutputStream(xml);
        } catch (IOException | FOPException | TransformerException e) {
            log.severe("Could not generate ERezept PDF:" + e);
            exceptionEvent.fireAsync(e);
            return new ByteArrayOutputStream();
        }
    }

    private File createTemporaryXmlFileFromBundles(List<BundleWithAccessCodeOrThrowable> bundles) throws IOException {
        String serialized = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root xmlns=\"http://hl7.org/fhir\">\n" +
                bundles.stream().filter(bundle -> bundle.getBundle() != null).map(bundle ->
                        "    <bundle>\n" +
                                "        <accessCode>" + bundle.getAccessCode() + "</accessCode>\n" +
                                "        " + ctx.newXmlParser().encodeResourceToString(bundle.getBundle()) + "\n" +
                                "    </bundle>")
                        .collect(Collectors.joining("\n")) +
                "\n</root>";

        File tmpFile = Files.createTempFile("bundle-", ".xml").toFile();
        Files.write(tmpFile.toPath(), serialized.getBytes(StandardCharsets.UTF_8));
        return tmpFile;
    }

    private ByteArrayOutputStream generatePdfInOutputStream(File xml) throws FOPException, TransformerException,
            IOException {
        // Step 2: Set up output stream.
        // Note: Using BufferedOutputStream for performance reasons (helpful with
        // FileOutputStreams).
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Step 3: Construct fop with desired output format
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

        // Step 4: Setup JAXP using identity transformer
        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        // with XSLT:
        String xslPath = "/fop/ERezeptTemplate.xsl";

        InputStream inputStream = getClass().getResourceAsStream(xslPath);
        String systemId = this.getClass().getResource(xslPath).toExternalForm();
        StreamSource xslt = new StreamSource(inputStream, systemId);
        xslt.setPublicId(systemId);
        factory.setErrorListener(new ErrorListener() {
            private static final String MSG = "Error in XSLT:";

            @Override
            public void warning(TransformerException exception) {
                log.warning(MSG + exception);

            }

            @Override
            public void fatalError(TransformerException exception) {
                log.severe(MSG + exception);

            }

            @Override
            public void error(TransformerException exception) {
                log.severe(MSG + exception);
            }
        });

        Transformer transformer = factory.newTransformer(xslt);
        transformer.setParameter("bundleFileUrl", xml.toURI().toURL().toString());

        // Step 5: Setup input and output for XSLT transformation
        // Setup input stream
        Source src = new StreamSource(xml);

        // Resulting SAX events (the generated FO) must be piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        // Step 6: Start XSLT transformation and FOP processing
        transformer.transform(src, res);
        return out;

    }

    /**
     * Used only to inject a mocked Event for tests
     *
     * @param eRezeptDocumentsEvent mocked Event
     */
    void seteRezeptDocumentsEvent(Event<ERezeptDocumentsEvent> eRezeptDocumentsEvent) {
        this.eRezeptDocumentsEvent = eRezeptDocumentsEvent;
    }
}