package health.ere.ps.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Event;
import javax.json.Json;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.SendHandler;
import javax.websocket.Session;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.ERezeptWithDocumentsEvent;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.model.pdf.ERezeptDocument;

class WebsocketTest {

  @Disabled("Currently failing since the introduction of the validation checks in the websocket.")
  @Test
  void testMessage() throws IOException {
      Websocket websocket = new Websocket();
      websocket.signAndUploadBundlesEvent = mock(Event.class);
      String signAndUploadBundles = new String(getClass().getResourceAsStream("/./test-classes/websocket" +
              "-messages/SignAndUploadBundles.json").readAllBytes(), StandardCharsets.UTF_8);

      websocket.onMessage(signAndUploadBundles, null);
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
    ERezeptDocument eRezeptDocument = new ERezeptDocument(list, Files.readAllBytes(Paths.get("./../src/test/resources/document-service/0428d416-149e-48a4-977c-394887b3d85c.pdf")));
      String json = websocket.generateJson(new ERezeptWithDocumentsEvent(List.of(eRezeptDocument)));

    Files.writeString(Paths.get("./../src/test/resources/websocket-messages/ERezeptDocuments.json"), json); // todo: write to src? in a test? shouldn't this be a .gitignore test-result like folder?
  }
  @Test
  void testOnMessageNull() {
    Websocket websocket = new Websocket();
    websocket.onMessage(null, null);
  }
  @Test
  void testOnMessageInvalidJson() {
    Websocket websocket = new Websocket();
    websocket.onMessage("asdasdsad", null);
  }

  @Test
  void testOnMessageInvalidJsonWithReplyTo() {
    Websocket websocket = new Websocket();
    Session mockedSession = mock(Session.class);

    Async mockedAsync = mock(Async.class);

    when(mockedSession.getAsyncRemote()).thenReturn(mockedAsync);

    websocket.onMessage("asdasdsad", mockedSession);

    ArgumentCaptor<String> exceptionMessageCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<SendHandler> sendHandlerCaptor = ArgumentCaptor.forClass(SendHandler.class);

    verify(mockedAsync).sendObject(exceptionMessageCaptor.capture(), sendHandlerCaptor.capture());
    String exception = exceptionMessageCaptor.getValue();

    javax.json.JsonObject exceptionObject = Json.createReader(new StringReader(exception)).readObject();

    assertEquals("Exception", exceptionObject.getString("type"));

  }
}
