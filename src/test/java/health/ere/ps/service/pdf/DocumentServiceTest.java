package health.ere.ps.service.pdf;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;

public class DocumentServiceTest {

	private DocumentService documentService;
	private final FhirContext ctx = FhirContext.forR4();

	@BeforeEach
	public void setUp() {
		documentService = new DocumentService();
		documentService.init();
	}


	//TODO: Real test with the downloaded pdf as expected output and a xml with the same info as input.

	@Test
	public void testGenerateERezeptPdf() throws IOException {
		// GIVEN
		Bundle bundle = (Bundle) ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));

		// WHEN
		ByteArrayOutputStream baos = documentService
				.generateERezeptPdf(
						Collections.singletonList(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_CODE")));
		Files.write(Paths.get("target/0428d416-149e-48a4-977c-394887b3d85c.pdf"), baos.toByteArray());
	}

	@Test
	public void testGenerateERezeptPdf2() throws IOException {
		// GIVEN
		Bundle bundle = (Bundle) ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/examples_erezept/154bdac4-9374-4276-9109-ea5cbdee84fc.xml"));

		// WHEN
		ByteArrayOutputStream baos = documentService.generateERezeptPdf(
				Collections.singletonList(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_CODE")));
		Files.write(Paths.get("target/154bdac4-9374-4276-9109-ea5cbdee84fc.pdf"), baos.toByteArray());
	}

	@Test
	public void testGenerateERezeptPdfMulti() throws IOException {
		// GIVEN
		Bundle bundle = (Bundle) ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/examples_erezept/154bdac4-9374-4276-9109-ea5cbdee84fc.xml"));

		Bundle bundle2 = (Bundle) ctx.newXmlParser().parseResource(
		getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));

		// WHEN
		ByteArrayOutputStream baos = documentService.generateERezeptPdf(Arrays.asList(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_CODE"), new BundleWithAccessCodeOrThrowable(bundle2, "MOCK_CODE2"), new BundleWithAccessCodeOrThrowable(bundle2, "MOCK_CODE3")));
		Files.write(Paths.get("target/MultiERezept.pdf"), baos.toByteArray());
	}
}