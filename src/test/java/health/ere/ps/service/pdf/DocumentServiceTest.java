package health.ere.ps.service.pdf;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;

public class DocumentServiceTest {
	@Test
	public void testGenerateERezeptPdf() throws IOException {
		DocumentService documentService = new DocumentService();
		documentService.init();
		Bundle bundle = (Bundle) documentService.ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));

		ByteArrayOutputStream baos = documentService
				.generateERezeptPdf(Arrays.asList(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_CODE")));
		Files.write(Paths.get("target/0428d416-149e-48a4-977c-394887b3d85c.pdf"), baos.toByteArray());
	}

	@Test
	public void testGenerateERezeptPdf2() throws IOException {
		DocumentService documentService = new DocumentService();
		documentService.init();
		Bundle bundle = (Bundle) documentService.ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/examples_erezept/154bdac4-9374-4276-9109-ea5cbdee84fc.xml"));

		ByteArrayOutputStream baos = documentService.generateERezeptPdf(Arrays.asList(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_CODE")));
		Files.write(Paths.get("target/154bdac4-9374-4276-9109-ea5cbdee84fc.pdf"), baos.toByteArray());
	}

	@Test
	public void testGenerateERezeptPdfMulti() throws IOException {
		DocumentService documentService = new DocumentService();
		documentService.init();
		Bundle bundle = (Bundle) documentService.ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/examples_erezept/154bdac4-9374-4276-9109-ea5cbdee84fc.xml"));

		Bundle bundle2 = (Bundle) documentService.ctx.newXmlParser().parseResource(
		getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));



		ByteArrayOutputStream baos = documentService.generateERezeptPdf(Arrays.asList(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_CODE"), new BundleWithAccessCodeOrThrowable(bundle2, "MOCK_CODE2"), new BundleWithAccessCodeOrThrowable(bundle2, "MOCK_CODE3")));
		Files.write(Paths.get("target/MultiERezept.pdf"), baos.toByteArray());
	}
}