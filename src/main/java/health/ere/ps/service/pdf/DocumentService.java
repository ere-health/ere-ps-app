package health.ere.ps.service.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.pdf.ERezeptDocument;
import health.ere.ps.websocket.ExceptionWithReplyToExcetion;

@ApplicationScoped
public class DocumentService {

    private static final Logger log = Logger.getLogger(DocumentService.class.getName());
    private static final int MAX_NUMBER_OF_MEDICINES_PER_PRESCRIPTIONS = 9;
    private final FhirContext ctx = FhirContext.forR4();

    @Inject
    Event<ERezeptDocumentsEvent> eRezeptDocumentsEvent;
    @Inject
    Event<Exception> exceptionEvent;

    @ConfigProperty(name = "ere.document-service.write-pdf-file", defaultValue = "false")
    boolean writePdfFile = false;

    private FopFactory fopFactory;

    @PostConstruct
    public void init() {
        try {
            URI baseURI = getClass().getResource("/fop/").toURI();
            FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(baseURI, new ClasspathResolverURIAdapter());
            initConfiguration(fopFactoryBuilder);
            fopFactory = fopFactoryBuilder.build();
            //fopFactory.getFontManager().setResourceResolver(new LoggingResolver(fopFactory.getFontManager().getResourceResolver()));
            //log.info(fopFactory.getFontManager().getResourceResolver().toString());
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
            // for windows replace \ with \\
            String absolutePath = physicalFile.getAbsolutePath().replace("\\", "\\\\");
            String config = Files.readString(new File(getClass().getResource("/fop/fop.xconf").toURI()).toPath())
                    .replaceAll("__WILL_BE_REPLACED_IN_DocumentService__", absolutePath);
            log.info("Config: "+config);
            cfg = new DefaultConfigurationBuilder().build(new ByteArrayInputStream(config.getBytes()));
            fopFactoryBuilder.setConfiguration(cfg);
        } catch (IllegalArgumentException | ConfigurationException | ArrayIndexOutOfBoundsException |
                IOException | URISyntaxException e) {
            if(e instanceof IllegalArgumentException) {
                String appFolder = "quarkus-app/app";
                if(!new File(appFolder).exists()) {
                    appFolder = "application/quarkus-app/app";
                }
                String appFolderWithFop = appFolder+"/fop";
                try {
                    if(!new File(appFolderWithFop).exists()) {
                        extractJarsFromFolderFopFolder(appFolder);
                    }
                    String config;
                    config = Files.readString(Paths.get(appFolder+"/fop/fop.xconf"))
                    .replaceAll("__WILL_BE_REPLACED_IN_DocumentService__", new File(appFolderWithFop+"/fonts/").getAbsolutePath().replace("\\", "\\\\"));
                    log.info("Config: "+config);
                    cfg = new DefaultConfigurationBuilder().build(new ByteArrayInputStream(config.getBytes()));
                    fopFactoryBuilder.setConfiguration(cfg);
                } catch (IOException | ConfigurationException e1) {
                    log.severe("Could not configure FOP from extracted jar: "+appFolder+"/fonts/fop.xconf:" + e);
                    exceptionEvent.fireAsync(e);
                }
            } else {
                log.severe("Could not configure FOP from file in classpath: /fonts/fop.xconf:" + e);
                exceptionEvent.fireAsync(e);
            }
        }
    }

    /**
     * https://stackoverflow.com/a/1529707/1059979
     * @param jarFile
     */
    void extractJarsFromFolderFopFolder(String folderWithJars) {
        Path dir = Paths.get(folderWithJars);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{jar}")) {
            for (Path entry: stream) {

                java.util.jar.JarFile jar;
                jar = new java.util.jar.JarFile(entry.toFile());

                log.info("Extracting "+entry.toFile().getAbsolutePath());
            
                java.util.Enumeration<JarEntry> enumEntries = jar.entries();
                while (enumEntries.hasMoreElements()) {
                    java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
                    if(!file.getName().startsWith("fop/")) {
                        continue;
                    }
                    java.io.File f = new java.io.File(folderWithJars+"/"+file.getName());
                    if (file.isDirectory()) { // if its a directory, create it
                        f.mkdir();
                        continue;
                    }
                    java.io.InputStream is = jar.getInputStream(file); // get the input stream
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
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
        log.info(String.format("About to create prescription receipts for %d bundles",
                bundlesWithAccessCodeEvent.getBundleWithAccessCodeOrThrowable().size()));
        bundlesWithAccessCodeEvent.getBundleWithAccessCodeOrThrowable().forEach(bundles -> {
            try {
                for (int i = 0; i < bundles.size(); i += MAX_NUMBER_OF_MEDICINES_PER_PRESCRIPTIONS) {
                    log.info(String.format("Processing bundle with %d medication(s)", i));
                    
                    List<BundleWithAccessCodeOrThrowable> subList = bundles
                        .subList(i, Math.min(i + MAX_NUMBER_OF_MEDICINES_PER_PRESCRIPTIONS, bundles.size()));
                    
                    ByteArrayOutputStream boas = new ByteArrayOutputStream();
                    if(!onlyContainsThrowables(subList)) {
                        log.info("Now creating prescription receipts");
                        try {
                            boas = generateERezeptPdf(subList);
                        } catch (IOException | FOPException | TransformerException e) {
                            log.severe("Could not generate ERezept PDF:" + e);
                            exceptionEvent.fireAsync(new ExceptionWithReplyToExcetion(e, bundlesWithAccessCodeEvent.getReplyTo(), bundlesWithAccessCodeEvent.getReplyToMessageId()));
                            boas = new ByteArrayOutputStream();
                        }
                    }
                
                    ERezeptDocument eRezeptDocument = new ERezeptDocument(subList, boas.size() > 0 ? boas.toByteArray() : null);
                
                    log.info("Created prescription receipts");
                    eRezeptDocumentsEvent.fireAsync(new ERezeptDocumentsEvent(List.of(eRezeptDocument),
                        bundlesWithAccessCodeEvent.getReplyTo(), bundlesWithAccessCodeEvent.getReplyToMessageId()));
                    log.info("Sending prescription receipts results.");
                }
            } catch (Exception ex) {
                exceptionEvent.fireAsync(new ExceptionWithReplyToExcetion(ex, bundlesWithAccessCodeEvent.getReplyTo(), bundlesWithAccessCodeEvent.getReplyToMessageId()));
            }
        });
    }

    private boolean onlyContainsThrowables(List<BundleWithAccessCodeOrThrowable> bundles) {
        return bundles.size() == bundles.stream().filter(bundle -> bundle.getThrowable() != null).count();
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

        if(isWritePdfFile()) {
            String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ssX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now());
            try {
                Path path = Paths.get(thisMoment+".pdf");
                log.info("Generating "+path.toAbsolutePath().toString());
                Files.write(path, out.toByteArray());
            } catch (IOException e) {
                log.log(Level.SEVERE, "Could not generate signature files", e);
            }
        }

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

    public boolean isWritePdfFile() {
        return this.writePdfFile;
    }
}