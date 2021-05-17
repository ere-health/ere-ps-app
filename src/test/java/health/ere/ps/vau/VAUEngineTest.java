package health.ere.ps.vau;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;

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
}
