package health.ere.ps.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class WebsocketXmlPrescriptionClientTest {
    @Test
    public void main() {
        try {
            // open websocket
            final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:8080/websocket"));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println(message);
                }
            });

            // send message to websocket
            String xmlBundle = new String(Files.readAllBytes(Paths.get("C:\\Users\\demo\\Downloads\\Kaiser_Bella_20210630113252.xml")));
            clientEndPoint.sendMessage("{'event':'XMLBundle','payload':'"+xmlBundle+"'}");

            // wait 5 seconds for messages from websocket
            // Thread.sleep(5000);

        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
