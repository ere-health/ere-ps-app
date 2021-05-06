package health.ere.ps.service.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class DocumentServiceTest {
	@Test
	public void testGenerateERezeptPdf() throws IOException {
		DocumentService documentService = new DocumentService();
		documentService.init();
		Bundle bundle = (Bundle) documentService.ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));

		ByteArrayOutputStream baos = documentService.generateERezeptPdf(bundle);
		Files.write(Paths.get("target/0428d416-149e-48a4-977c-394887b3d85c.pdf"), baos.toByteArray());
	}

	@Test
	public void testGenerateERezeptPdf2() throws IOException {
		DocumentService documentService = new DocumentService();
		documentService.init();
		Bundle bundle = (Bundle) documentService.ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/examples_erezept/154bdac4-9374-4276-9109-ea5cbdee84fc.xml"));

		ByteArrayOutputStream baos = documentService.generateERezeptPdf(bundle);
		Files.write(Paths.get("target/154bdac4-9374-4276-9109-ea5cbdee84fc.pdf"), baos.toByteArray());
	}
}