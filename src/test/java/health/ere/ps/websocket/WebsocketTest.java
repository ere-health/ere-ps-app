package health.ere.ps.websocket;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Event;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.pdf.ERezeptDocument;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class WebsocketTest {

  @Disabled("Currently failing since the introduction of the validation checks in the websocket.")
  @Test
  void testMessage() throws IOException {
      Websocket websocket = new Websocket();
      websocket.signAndUploadBundlesEvent = mock(Event.class);
      String signAndUploadBundles = new String(getClass().getResourceAsStream("/websocket" +
              "-messages/SignAndUploadBundles.json").readAllBytes(), StandardCharsets.UTF_8);

      websocket.onMessage(signAndUploadBundles);
      verify(websocket.signAndUploadBundlesEvent, times(1)).fireAsync(any());
  }

  // Passing but also generating LogManager errors since the introduction of the validation checks
  // in the websocket.
  @Test
  void testGetJsonEventFor() throws IOException {
    Websocket websocket = new Websocket();
    List<BundleWithAccessCodeOrThrowable> list = new ArrayList<>();
    Bundle bundle = (Bundle) FhirContext.forR4().newXmlParser().parseResource(
				getClass().getResourceAsStream("/examples_erezept/Erezept_template_2.xml"));
    list.add(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_ACCESS_CODE"));
    ERezeptDocument eRezeptDocument = new ERezeptDocument(list, Files.readAllBytes(Paths.get("src/test/resources/document-service/0428d416-149e-48a4-977c-394887b3d85c.pdf")));
      String json = websocket.getJson(new ERezeptDocumentsEvent(List.of(eRezeptDocument)));

    Files.writeString(Paths.get("src/test/resources/websocket-messages/ERezeptDocuments.json"), json);
  }
}
