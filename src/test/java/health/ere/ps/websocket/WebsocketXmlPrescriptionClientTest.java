package health.ere.ps.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
