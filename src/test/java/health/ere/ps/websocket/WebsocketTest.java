package health.ere.ps.websocket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.enterprise.event.Event;

import org.junit.jupiter.api.Test;

class WebsocketTest {
  @Test
  void testMessage() throws IOException {
      Websocket websocket = new Websocket();
      websocket.signAndUploadBundlesEvent = mock(Event.class);
      String signAndUploadBundles = new String(getClass().getResourceAsStream("/websocket-messages/SignAndUploadBundles.json").readAllBytes());
      websocket.onMessage(signAndUploadBundles);
      verify(websocket.signAndUploadBundlesEvent, times(1)).fireAsync(any());
  }  
}
