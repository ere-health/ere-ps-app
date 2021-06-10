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


	@Test
	public void generateERezeptPdf_generatesCorrectPdf_givenOneMedicineToDisplay() throws IOException {
		// GIVEN
		Bundle bundle = (Bundle) ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/examples_erezept/Erezept_template_with_one_medicine.xml"));

		// WHEN + THEN
		ByteArrayOutputStream baos = documentService.generateERezeptPdf(
				Collections.singletonList(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_CODE")));
		Files.write(Paths.get("target/Erezept_with_one_medicine.pdf"), baos.toByteArray());
	}

	@Test
	public void testGenerateERezeptPdfMulti() throws IOException {
		// GIVEN
		Bundle bundle = (Bundle) ctx.newXmlParser().parseResource(
				getClass().getResourceAsStream("/examples_erezept/Erezept_template_with_one_medicine.xml"));

		Bundle bundle2 = (Bundle) ctx.newXmlParser().parseResource(
		getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));

		// WHEN + THEN
		ByteArrayOutputStream baos = documentService.generateERezeptPdf(Arrays.asList(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_CODE"), new BundleWithAccessCodeOrThrowable(bundle2, "MOCK_CODE2"), new BundleWithAccessCodeOrThrowable(bundle2, "MOCK_CODE3")));
		Files.write(Paths.get("target/MultiERezept.pdf"), baos.toByteArray());
	}
}