package health.ere.ps.vau;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

class VAUEngineTest {

    private static Logger log = Logger.getLogger(VAUEngineTest.class.getName());

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
        vauEngine.requestidThreadLocal.set("1c51e243bf3f657b8f9d0034e30aac40");
        HttpResponse res = vauEngine.extractHttpResponse(testResponse);
        assertEquals(401, res.getStatusLine().getStatusCode());
        assertEquals("application/fhir+xml", res.getFirstHeader("content-type").getValue());
        assertEquals("<OperationOutcome xmlns=\"http://hl7.org/fhir\"><meta><profile value=\"http://hl7.org/fhir/StructureDefinition/OperationOutcome\"/></meta><issue><severity value=\"error\"/><code value=\"unknown\"/><details><text value=\"Access Token Error: Expired!\"/></details></issue></OperationOutcome>", new String(res.getEntity().getContent().readAllBytes()));
    }
}
