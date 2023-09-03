package health.ere.ps.websocket;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebsocketXmlPrescriptionClientTest {

    @Test
    @Disabled
    public void testVOSBundleERE614() {

        String jsonBundle;
        try {
            jsonBundle = new String(Files.readAllBytes(Paths.get("../vos-erp-translator/src/test/resources/websocket/ERE-614.json")));
            
            jsonBundle = jsonBundle.replaceFirst("a99442c7-e26f-4101-ba53-598e214c9a0c", UUID.randomUUID().toString());
            jsonBundle = jsonBundle.replaceAll("2023-08-21T15:21:50\\+02:00", Instant.now().toString());
            jsonBundle = jsonBundle.replaceAll("2023-08-21", Instant.now().toString().substring(0, 10));
            

            sendMessage(jsonBundle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    public void testVOSBundleERE614List() {

        String jsonBundle;
        try {
            jsonBundle = new String(Files.readAllBytes(Paths.get("../vos-erp-translator/src/test/resources/websocket/ERE-614-list-of-list.json")));
            
            jsonBundle = jsonBundle.replaceFirst("a99442c7-e26f-4101-ba53-598e214c9a0c", UUID.randomUUID().toString());
            jsonBundle = jsonBundle.replaceAll("2023-08-21T15:21:50\\+02:00", Instant.now().toString());
            jsonBundle = jsonBundle.replaceAll("2023-08-21", Instant.now().toString().substring(0, 10));
            

            sendMessage(jsonBundle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    public void testVOSBundle() {

        String jsonBundle;
        try {
            jsonBundle = new String(Files.readAllBytes(Paths.get("../vos-erp-translator/src/test/resources/websocket/ere.json")));
            
            jsonBundle = jsonBundle.replaceFirst("a152qv21-9851-701o-32vx-9q3a3c5r91tf", UUID.randomUUID().toString());
            jsonBundle = jsonBundle.replaceAll("2021-09-23T13:27:58\\+02:00", Instant.now().toString());
            

            sendMessage(jsonBundle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    public void testVOSNewProfileBundle() {

        String jsonBundle;
        try {
            jsonBundle = new String(Files.readAllBytes(Paths.get("../vos-erp-translator/src/test/resources/websocket/ERE-612.json")));
            
            jsonBundle = jsonBundle.replaceFirst("3cc0e077-92c0-4a35-b856-8fa35b2aba4d", UUID.randomUUID().toString());
            jsonBundle = jsonBundle.replaceAll("2023-05-22T02:00:00\\+02:00", Instant.now().toString());
            

            sendMessage(jsonBundle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    public void testVOSBundleWithError() {

        String jsonBundle;
        try {
            jsonBundle = new String(Files.readAllBytes(Paths.get("../vos-erp-translator/src/test/resources/websocket/bundle-with-validation-error.json")));
            
            jsonBundle = jsonBundle.replaceFirst("2c62cdee-d58a-4001-9bc9-isudhfg78345", UUID.randomUUID().toString());
            jsonBundle = jsonBundle.replaceAll("2022-10-27T02:00:00\\+02:00", Instant.now().toString());

            sendMessage(jsonBundle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    public void testXmlPrescriptionSignAndUploadBundles() {

        String jsonBundle;
        try {
            jsonBundle = new String(Files.readAllBytes(Paths.get("src/test/resources/websocket-messages/SignAndUploadBundles.json")));
            
            jsonBundle = jsonBundle.replaceFirst("0428d416-149e-48a4-977c-394887b3d85c", UUID.randomUUID().toString());

            sendMessage(jsonBundle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    public void testXmlPrescriptionSignAndUploadBundlesWithRuntimeConfig() {

        String jsonBundle;
        try {
            jsonBundle = new String(Files.readAllBytes(Paths.get("src/test/resources/websocket-messages/SignAndUploadBundles-With-RuntimeConfig.json")));
            
            jsonBundle = jsonBundle.replaceFirst("0428d416-149e-48a4-977c-394887b3d85c", UUID.randomUUID().toString());

            sendMessage(jsonBundle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    public void testGetCardsWithRuntimeConfig() {

        String jsonBundle;
        try {
            jsonBundle = new String(Files.readAllBytes(Paths.get("src/test/resources/websocket-messages/GetCards-With-RuntimeConfig.json")));
            
            sendMessage(jsonBundle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Test
    @Disabled("This test case needs unpublished data")
    public void testXmlPrescription() {

        String xmlBundle;
        try {
            xmlBundle = new String(Files.readAllBytes(Paths.get("../secret-test-print-samples/CGM-Turbomed/XML/Kaiser_Bella_20210630113252.xml")));
            
            xmlBundle = xmlBundle.replaceFirst("c23a81e7-8ec1-4a6f-9f35-0e7e0b9e1dc7", UUID.randomUUID().toString());

            xmlBundle = xmlBundle.replaceFirst("4707031e-8592-45b0-8687-95df80d77a21", UUID.randomUUID().toString());

            xmlBundle = xmlBundle.replaceFirst("93e12b90-26ff-40b0-ac10-1cbb29be04ea", UUID.randomUUID().toString());

            String message = "{\"type\":\"XMLBundle\",\"payload\":"+new ObjectMapper().writeValueAsString(xmlBundle)+"}";
            sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled("This test case needs unpublished data")
    public void testPublishERezeptWithDocuments() {

        String eRezeptDocuments;
        try {
            eRezeptDocuments = new String(Files.readAllBytes(Paths.get("src/test/resources/websocket-messages/ERezeptWithDocuments-2.json")));
            String message = "{\"type\":\"Publish\",\"payload\":"+new ObjectMapper().writeValueAsString(eRezeptDocuments)+"}";
            sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            // open websocket
            final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:8080/websocket"));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println(message);
                    JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
                    if(jsonObject.getString("type").equals("HTMLBundles")) {
                        JsonArray ja = jsonObject.getJsonArray("payload");
                        for(JsonValue jv : ja) {
                            JsonString js = (JsonString) jv;
                            try {
                                Files.write(Paths.get("target/HTML-Bundle-"+UUID.randomUUID().toString()+".html"), js.getString().getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

            Thread.sleep(1000);

            // send message to websocket
           clientEndPoint.sendMessage(message);
            

            Thread.sleep(30000);

        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
