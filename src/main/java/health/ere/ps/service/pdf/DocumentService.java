package health.ere.ps.service.pdf;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.ERezeptWithDocumentsEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.pdf.ERezeptDocument;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.service.transformer.XmlTransformerProvider;
import health.ere.ps.websocket.ExceptionWithReplyToException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Bundle;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static java.time.ZoneOffset.UTC;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.apache.xmlgraphics.util.MimeConstants.MIME_PDF;

@ApplicationScoped
public class DocumentService {

    private static final Logger log = Logger.getLogger(DocumentService.class.getName());
    
    private static final int MAX_NUMBER_OF_MEDICINES_PER_PRESCRIPTIONS = 9;
    private static final FhirContext fhirContext = FHIRService.getFhirContext();

    IParser jsonParser = fhirContext.newJsonParser();
    IParser xmlParser = fhirContext.newXmlParser();

    @Inject
    Event<ERezeptWithDocumentsEvent> eRezeptDocumentsEvent;

    @Inject
    Event<Exception> exceptionEvent;

    @Inject
    XmlTransformerProvider xmlTransformerProvider;

    @ConfigProperty(name = "ere.document-service.write-pdf-file", defaultValue = "false")
    boolean writePdfFile;

    private FopFactory fopFactory;

    @PostConstruct
    public void init() {
        try {
            URI baseURI = getClass().getResource("/fop/").toURI();
            FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(baseURI, new ClasspathResolverURIAdapter());
            initConfiguration(fopFactoryBuilder);
            fopFactory = fopFactoryBuilder.build();
        } catch (Exception ex) {
            log.log(SEVERE, "FOP Factory cannot be initialized", ex);
            exceptionEvent.fireAsync(ex);
        }
    }

    private void initConfiguration(FopFactoryBuilder fopFactoryBuilder) {
        Configuration cfg;
        try {
            URI uri = getClass().getResource("/fop/fonts/").toURI();
            File physicalFile = new File(uri);
            // for windows replace \ with \\
            String absolutePath = physicalFile.getAbsolutePath().replace("\\", "\\\\");
            String config = Files.readString(new File(getClass().getResource("/fop/fop.xconf").toURI()).toPath())
                .replaceAll("__WILL_BE_REPLACED_IN_DocumentService__", absolutePath);
            log.info("Config: " + config);
            cfg = new DefaultConfigurationBuilder().build(new ByteArrayInputStream(config.getBytes()));
            fopFactoryBuilder.setConfiguration(cfg);
        } catch (IllegalArgumentException | ConfigurationException | ArrayIndexOutOfBoundsException |
                 IOException | URISyntaxException e) {
            if (e instanceof IllegalArgumentException) {
                String appFolder = "quarkus-app/app";
                if (!new File(appFolder).exists()) {
                    appFolder = "application/quarkus-app/app";
                }
                String appFolderWithFop = appFolder + "/fop";
                try {
                    if (!new File(appFolderWithFop).exists()) {
                        extractJarsFromFolderFopFolder(appFolder);
                    }
                    File fonts = new File(appFolderWithFop + "/fonts/");
                    String config = Files
                        .readString(Paths.get(appFolder + "/fop/fop.xconf"))
                        .replaceAll("__WILL_BE_REPLACED_IN_DocumentService__", fonts.getAbsolutePath().replace("\\", "\\\\"));
                    log.info("Config: " + config);
                    cfg = new DefaultConfigurationBuilder().build(new ByteArrayInputStream(config.getBytes()));
                    fopFactoryBuilder.setConfiguration(cfg);
                } catch (IOException | ConfigurationException e1) {
                    log.severe("Could not configure FOP from extracted jar: " + appFolder + "/fonts/fop.xconf:" + e);
                    exceptionEvent.fireAsync(e);
                }
            } else {
                log.severe("Could not configure FOP from file in classpath: /fonts/fop.xconf:" + e);
                exceptionEvent.fireAsync(e);
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/a/1529707/1059979">...</a>
     */
    void extractJarsFromFolderFopFolder(String folderWithJars) {
        Path dir = Paths.get(folderWithJars);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{jar}")) {
            for (Path entry : stream) {
                JarFile jar = new JarFile(entry.toFile());
                log.info("Extracting " + entry.toFile().getAbsolutePath());

                Enumeration<JarEntry> enumEntries = jar.entries();
                while (enumEntries.hasMoreElements()) {
                    JarEntry file = enumEntries.nextElement();
                    if (!file.getName().startsWith("fop/")) {
                        continue;
                    }
                    File f = new File(folderWithJars + "/" + file.getName());
                    if (file.isDirectory()) { // if it's a directory, create it
                        f.mkdir();
                        continue;
                    }
                    InputStream is = jar.getInputStream(file); // get the input stream
                    FileOutputStream fos = new FileOutputStream(f);
                    is.transferTo(fos);
                    fos.close();
                    is.close();
                }
                jar.close();
            }
        } catch (DirectoryIteratorException | IOException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            log.severe("Could not extract jar " + ex);
            exceptionEvent.fireAsync(ex);
        }
    }

    public void onBundlesWithAccessCodes(@ObservesAsync BundlesWithAccessCodeEvent bundlesWithAccessCodeEvent) {
        int size = bundlesWithAccessCodeEvent.getBundleWithAccessCodeOrThrowable().size();
        log.info(String.format("About to create prescription receipts for %d bundles", size));
        bundlesWithAccessCodeEvent.getBundleWithAccessCodeOrThrowable().forEach(bundles -> {
            try {
                for (int i = 0; i < bundles.size(); i += MAX_NUMBER_OF_MEDICINES_PER_PRESCRIPTIONS) {
                    log.info(String.format("Processing bundle with %d medication(s)", i));

                    List<BundleWithAccessCodeOrThrowable> subList = bundles
                        .subList(i, Math.min(i + MAX_NUMBER_OF_MEDICINES_PER_PRESCRIPTIONS, bundles.size()));

                    ERezeptDocument eRezeptDocument = null;
                    if (!containsThrowables(subList)) {
                        log.info("Now creating prescription receipts");
                        try {
                            ByteArrayOutputStream os = generateERezeptPdf(subList);
                            eRezeptDocument = new ERezeptDocument(subList, os.size() > 0 ? os.toByteArray() : null);
                        } catch (IOException | FOPException | TransformerException e) {
                            log.severe("Could not generate ERezept PDF:" + e);
                            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, bundlesWithAccessCodeEvent.getReplyTo(), bundlesWithAccessCodeEvent.getReplyToMessageId()));
                        }
                    } else {
                        log.warning("E-Prescriptions contain throwables. Will not generate PDF.");
                        eRezeptDocument = new ERezeptDocument(subList, null);
                    }
                    log.info("Created prescription receipts");
                    ERezeptWithDocumentsEvent documentsEvent = new ERezeptWithDocumentsEvent(
                        eRezeptDocument != null ? List.of(eRezeptDocument) : new ArrayList<>(),
                        bundlesWithAccessCodeEvent.getReplyTo(),
                        bundlesWithAccessCodeEvent.getReplyToMessageId());
                    eRezeptDocumentsEvent.fireAsync(documentsEvent);
                    log.info("Sending prescription receipts results.");
                }
            } catch (Exception ex) {
                exceptionEvent.fireAsync(new ExceptionWithReplyToException(ex, bundlesWithAccessCodeEvent.getReplyTo(), bundlesWithAccessCodeEvent.getReplyToMessageId()));
            }
        });
    }

    private boolean containsThrowables(List<BundleWithAccessCodeOrThrowable> bundles) {
        return bundles.stream().anyMatch(bundle -> bundle.getThrowable() != null);
    }

    public BundleWithAccessCodeOrThrowable convert(JsonValue jv) {
        BundleWithAccessCodeOrThrowable bt = new BundleWithAccessCodeOrThrowable();
        if (jv instanceof JsonObject jo) {
            if (jo.containsKey("accessCode")) {
                bt.setAccessCode(jo.getString("accessCode"));
            }
            String mimeType = jo.getString("mimeType", APPLICATION_JSON);
            try {
                if (APPLICATION_XML.equals(mimeType)) {
                    bt.setBundle(xmlParser.parseResource(Bundle.class, jo.getJsonString("bundle").getString()));
                } else {
                    bt.setBundle(jsonParser.parseResource(Bundle.class, jo.getJsonObject("bundle").toString()));
                }
            } catch (Throwable t) {
                log.log(WARNING, "Could not extract taskId and/or medicationRequest Id from Bundle", t);
            }
        }
        return bt;
    }

    public ByteArrayOutputStream generateERezeptPdf(List<BundleWithAccessCodeOrThrowable> bundles) throws IOException, FOPException, TransformerException {
        if (bundles.isEmpty()) {
            log.severe("Cannot generate prescriptions pdf for an empty bundle");
            return new ByteArrayOutputStream();
        }
        File xml = createTemporaryXmlFileFromBundles(bundles);
        return generatePdfInOutputStream(xml);
    }

    private File createTemporaryXmlFileFromBundles(List<BundleWithAccessCodeOrThrowable> bundles) throws IOException {
        String serialized = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root xmlns=\"http://hl7.org/fhir\">\n" +
            bundles.stream().filter(bundle -> bundle.getBundle() != null).map(bundle ->
                    "    <bundle>\n" +
                        "        <accessCode>" + bundle.getAccessCode() + "</accessCode>\n" +
                        "        " + fhirContext.newXmlParser().encodeResourceToString(bundle.getBundle()) + "\n" +
                        "    </bundle>")
                .collect(Collectors.joining("\n")) +
            "\n</root>";

        File tmpFile = Files.createTempFile("bundle-", ".xml").toFile();
        Files.writeString(tmpFile.toPath(), serialized);
        return tmpFile;
    }

    private ByteArrayOutputStream generatePdfInOutputStream(File xml) throws FOPException, TransformerException, IOException {
        // Step 1: Set up output stream.
        // Note: Using BufferedOutputStream for performance reasons (helpful with
        // FileOutputStreams).
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Step 2: Construct fop with desired output format
        Fop fop = fopFactory.newFop(MIME_PDF, out);

        Transformer transformer = xmlTransformerProvider.getTransformer("/fop/ERezeptTemplate.xsl");
        transformer.setParameter("bundleFileUrl", xml.toURI().toURL().toString());

        // Step 3: Setup input and output for XSLT transformation
        // Setup input stream
        Source src = new StreamSource(xml);

        // Resulting SAX events (the generated FO) must be piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        // Step 4: Start XSLT transformation and FOP processing
        transformer.transform(src, res);

        if (isWritePdfFile()) {
            String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX").withZone(UTC).format(Instant.now());
            try {
                Path path = Paths.get(thisMoment + ".pdf");
                log.info("Generating " + path.toAbsolutePath().toString());
                Files.write(path, out.toByteArray());
            } catch (IOException e) {
                log.log(SEVERE, "Could not generate signature files", e);
            }
        }
        return out;
    }

    /**
     * Used only to inject a mocked Event for tests
     *
     * @param eRezeptDocumentsEvent mocked Event
     */
    void setErezeptDocumentsEvent(Event<ERezeptWithDocumentsEvent> eRezeptDocumentsEvent) {
        this.eRezeptDocumentsEvent = eRezeptDocumentsEvent;
    }

    public boolean isWritePdfFile() {
        return writePdfFile;
    }
}