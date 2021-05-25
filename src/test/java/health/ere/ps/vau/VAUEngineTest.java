package health.ere.ps.vau;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import health.ere.ps.vau.VAUEngine;

class VAUEngineTest {

    private static Logger log = Logger.getLogger(VAUEngineTest.class.getName());

    @Test @Disabled
    void testVauEngine() throws URISyntaxException {
        ResteasyClient vauEnabledClient = new ResteasyClientBuilderImpl()
                .httpEngine(new VAUEngine(new URI("http://localhost:9094/services/handshake"))).build();
        String helloSoapRequest = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n"+
        "  <soap:Body>\n"+
        "    <ns2:sayHello xmlns:ns2=\"http://cxf.vauchannel.ti.gematik.de/\">\n"+
        "      <arg0>hello from integration client</arg0>\n"+
        "    </ns2:sayHello>\n"+
        "  </soap:Body>\n"+
        "</soap:Envelope>";
        String s = vauEnabledClient.target("http://localhost:9094/services").request().post(Entity.entity(helloSoapRequest, " application/soap+xml; charset=UTF-8")).readEntity(String.class);
        log.info(s);
    }


    @Test
    public void testParseResponseFromVAU() throws IOException, HttpException {
        String testResponse = "1 1c51e243bf3f657b8f9d0034e30aac40 HTTP/1.1 401 Unauthorized\n"+
        "content-length: 279\n"+
        "connection: close\n"+
        "www-authenticate: Bearer realm='prescriptionserver.telematik', error='invalACCESS_TOKEN'\n"+
        "content-type: application/fhir+xml\n"+
        "date: Mon, 24 May 2021 12:40:10 GMT\n"+
        "\n"+
        "<OperationOutcome xmlns=\"http://hl7.org/fhir\"><meta><profile value=\"http://hl7.org/fhir/StructureDefinition/OperationOutcome\"/></meta><issue><severity value=\"error\"/><code value=\"unknown\"/><details><text value=\"Access Token Error: Expired!\"/></details></issue></OperationOutcome>";
        VAUEngine vauEngine = new VAUEngine("");
        vauEngine.requestid = "1c51e243bf3f657b8f9d0034e30aac40";
        HttpResponse res = vauEngine.extractHttpResponse(testResponse);
        assertEquals(401, res.getStatusLine().getStatusCode());
        assertEquals("application/fhir+xml", res.getFirstHeader("content-type").getValue());
        assertEquals("<OperationOutcome xmlns=\"http://hl7.org/fhir\"><meta><profile value=\"http://hl7.org/fhir/StructureDefinition/OperationOutcome\"/></meta><issue><severity value=\"error\"/><code value=\"unknown\"/><details><text value=\"Access Token Error: Expired!\"/></details></issue></OperationOutcome>", new String(res.getEntity().getContent().readAllBytes()));
    }
}
