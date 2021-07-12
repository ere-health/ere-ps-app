package health.ere.ps.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class WebsocketXmlPrescriptionClientTest {
    @Test
    @Disabled("This test case needs unpublished data")
    public void testXmlPrescription() {
        try {
            // open websocket
            final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:8080/websocket"));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println(message);
                }
            });

            Thread.sleep(1000);

            // send message to websocket
            String xmlBundle;
            try {
                xmlBundle = new String(Files.readAllBytes(Paths.get("../secret-test-print-samples/CGM-Turbomed/XML/Kaiser_Bella_20210630113252.xml")));
                
                xmlBundle = xmlBundle.replaceFirst("c23a81e7-8ec1-4a6f-9f35-0e7e0b9e1dc7", UUID.randomUUID().toString());

                xmlBundle = xmlBundle.replaceFirst("4707031e-8592-45b0-8687-95df80d77a21", UUID.randomUUID().toString());

                xmlBundle = xmlBundle.replaceFirst("93e12b90-26ff-40b0-ac10-1cbb29be04ea", UUID.randomUUID().toString());

                clientEndPoint.sendMessage("{\"type\":\"XMLBundle\",\"payload\":"+new ObjectMapper().writeValueAsString(xmlBundle)+"}");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Thread.sleep(1000);

        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}