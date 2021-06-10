package health.ere.ps.websocket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Event;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.pdf.ERezeptDocument;

class WebsocketTest {
  @Test
  void testMessage() throws IOException {
      Websocket websocket = new Websocket();
      websocket.signAndUploadBundlesEvent = mock(Event.class);
      String signAndUploadBundles = new String(getClass().getResourceAsStream("/websocket-messages/SignAndUploadBundles.json").readAllBytes());
      websocket.onMessage(signAndUploadBundles);
      verify(websocket.signAndUploadBundlesEvent, times(1)).fireAsync(any());
  }  

  @Test
  void testGetJsonEventFor() throws IOException {
    Websocket websocket = new Websocket();
    ERezeptDocumentsEvent eRezeptDocumentsEvent = new ERezeptDocumentsEvent();
    List<BundleWithAccessCodeOrThrowable> list = new ArrayList<>();
    Bundle bundle = (Bundle) FhirContext.forR4().newXmlParser().parseResource(
				getClass().getResourceAsStream("/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml"));
    list.add(new BundleWithAccessCodeOrThrowable(bundle, "MOCK_ACCESS_CODE"));
    ERezeptDocument eRezeptDocument = new ERezeptDocument(list, Files.readAllBytes(Paths.get("src/test/resources/document-service/0428d416-149e-48a4-977c-394887b3d85c.pdf")));
    eRezeptDocumentsEvent.getERezeptWithDocuments().add(eRezeptDocument);
    String json = websocket.getJson(eRezeptDocumentsEvent);

    Files.writeString(Paths.get("src/test/resources/websocket-messages/ERezeptDocuments.json"), json);
  }
}
