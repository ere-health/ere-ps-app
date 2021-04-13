package com.erehealth.ps.service.pdf;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.fop.apps.*;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.apache.fop.configuration.DefaultConfiguration;
import org.hl7.fhir.r4.model.Bundle;
import org.xml.sax.SAXException;

import ca.uhn.fhir.context.FhirContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class DocumentService {

	private static final Logger log = Logger.getLogger(DocumentService.class.getName());

	FopFactory fopFactory;

	// Create a FHIR context
	FhirContext ctx = FhirContext.forR4();

	public DocumentService() {

	}

	@PostConstruct
	public void init() {
		try {
			URI baseURI = getClass().getResource("/fop/").toURI();
			FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(baseURI);
			initConfiguration(fopFactoryBuilder);
			fopFactory = fopFactoryBuilder.build();
		} catch (URISyntaxException ex) {
			log.log(Level.SEVERE, "FOP Factory not initializable.", ex);
		}
	}

	private void initConfiguration(FopFactoryBuilder fopFactoryBuilder) throws URISyntaxException {
		Configuration cfg;
		try {
			cfg = new DefaultConfigurationBuilder().build(getClass().getResourceAsStream("/fop/fop.xconf"));
			URI uri = getClass().getResource("/fop/fonts/").toURI();
			File physicalFile = new File(uri);
			String absolutePath = physicalFile.getAbsolutePath();
			List<String> fonts = Arrays.asList("arial.ttf", "arialbd.ttf", "arialbi.ttf", "ariali.ttf", "ARIALN.TTF",
					"ARIALNB.TTF", "ARIALNBI.TTF", "ARIALNI.TTF", "ARIALUNI.TTF", "ARIALUNIB.TTF", "ariblk.ttf",
					"Symbola.ttf");
			for (String font : fonts) {
				uri = getClass().getResource("/fop/fonts/" + font).toURI();
				log.info("Font found: " + uri);
			}
			// log.log(Level.INFO, "Setting fonts path to: {0}", absolutePath);
			// ((DefaultConfiguration)
			// cfg.getChildren("renderers")[0].getChildren("renderer")[0]
			// .getChildren("fonts")[0].getChildren("directory")[0])
			// .setValue(absolutePath);

			fopFactoryBuilder.setConfiguration(cfg);
		} catch (IllegalArgumentException | ConfigurationException | ArrayIndexOutOfBoundsException e) {
			log.log(Level.WARNING, "Could not configure FOP from file in classpath: /fonts/fop.xconf", e);
		}
	}

	ByteArrayOutputStream generatePdfInOutputStream(File xml)
			throws FOPException, TransformerFactoryConfigurationError, TransformerException, IOException {
		// Step 2: Set up output stream.
		// Note: Using BufferedOutputStream for performance reasons (helpful with
		// FileOutputStreams).
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		// Step 3: Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

		// Step 4: Setup JAXP using identity transformer
		TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
		try {
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		} catch (IllegalArgumentException e) {
			log.log(Level.FINE, "Features not supported!");
		}

		// without XSLT:
		// Transformer transformer = factory.newTransformer(); // identity transformer

		// with XSLT:

		String xslPath = "/fop/ERezeptTemplate.xsl";

		InputStream inputStream = getClass().getResourceAsStream(xslPath);
		String systemId = this.getClass().getResource(xslPath).toExternalForm();
		StreamSource xslt = new StreamSource(inputStream, systemId);
		xslt.setPublicId(systemId);
		factory.setErrorListener(new ErrorListener() {
			private static final String MSG = "Warning in XSLT";

			@Override
			public void warning(TransformerException exception) throws TransformerException {
				log.log(Level.WARNING, MSG, exception);

			}

			@Override
			public void fatalError(TransformerException exception) throws TransformerException {
				log.log(Level.SEVERE, MSG, exception);

			}

			@Override
			public void error(TransformerException exception) throws TransformerException {
				log.log(Level.SEVERE, MSG, exception);
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

	File createTemporaryXmlFileFromBundle(Bundle bundle) throws IOException {
		Path applicationTempPath = Files.createTempFile("bundle-", ".xml");
		File tmpFile = applicationTempPath.toFile();
		String serialized = ctx.newXmlParser().encodeResourceToString(bundle);
		Files.write(tmpFile.toPath(), serialized.getBytes());
		return tmpFile;
	}

	public ByteArrayOutputStream generateERezeptPdf(Bundle bundle) {
		File xml;
		try {
			xml = createTemporaryXmlFileFromBundle(bundle);
			return generatePdfInOutputStream(xml);
		} catch (IOException | FOPException | TransformerFactoryConfigurationError | TransformerException e) {
			log.log(Level.SEVERE, "Could not generate ERezept PDF", e);
			return null;
		}
	}
}