package health.ere.ps.service.health;

import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.health.service.cetp.CETPServer;
import health.ere.ps.profile.RUDevTestProfile;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.idp.BearerTokenService;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Optional;

import static health.ere.ps.service.health.check.Check.CARDLINK_WEBSOCKET_CHECK;
import static health.ere.ps.service.health.check.Check.CETP_SERVER_CHECK;
import static health.ere.ps.service.health.check.Check.GIT_CHECK;
import static health.ere.ps.service.health.check.Check.STATUS_CHECK;
import static io.restassured.RestAssured.given;
import static org.apache.http.params.CoreConnectionPNames.CONNECTION_TIMEOUT;
import static org.apache.http.params.CoreConnectionPNames.SO_TIMEOUT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(RUDevTestProfile.class)
class HealthCheckTest {

    @Inject
    BearerTokenService bearerTokenService;

    @Inject
    MultiConnectorServicesProvider multiConnectorServicesProvider;

    @BeforeEach
    void beforeEach() {
        MultiConnectorServicesProvider connectorServicesProvider = mock(MultiConnectorServicesProvider.class);
        when(connectorServicesProvider.getContextType(any())).thenReturn(new ContextType());
        when(connectorServicesProvider.getEventServicePortType(any())).thenReturn(mock(EventServicePortType.class));
        when(connectorServicesProvider.getCertificateServicePortType(any())).thenReturn(mock(CertificateServicePortType.class));

        QuarkusMock.installMockForType(connectorServicesProvider, MultiConnectorServicesProvider.class);

        BearerTokenService mockTokenService = mock(BearerTokenService.class);

        QuarkusMock.installMockForType(mockTokenService, BearerTokenService.class);
    }

    @AfterEach
    void afterAll() {
        QuarkusMock.installMockForType(bearerTokenService, BearerTokenService.class);
        QuarkusMock.installMockForType(multiConnectorServicesProvider, MultiConnectorServicesProvider.class);
    }

    @Test
    void healthCheckWorks() throws Exception {
        RestAssuredConfig config = RestAssured.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                .setParam(CONNECTION_TIMEOUT, 10000)
                .setParam(SO_TIMEOUT, 10000));

        connectToCetpServer();

        Response response = given().header(new Header("X-eHBAHandle", "test")).config(config).when().get("/health");
        response.then().statusCode(200);
        HealthInfo healthInfo = response.getBody().as(HealthInfo.class);
        assertThat(healthInfo.checks().size(), equalTo(4));
        Optional<CheckInfo> cardLinkWebsocketCheckOpt = healthInfo.checks()
            .stream()
            .filter(check -> check.name().equals(CARDLINK_WEBSOCKET_CHECK))
            .findFirst();
        assertTrue(cardLinkWebsocketCheckOpt.isPresent());
        assertThat(cardLinkWebsocketCheckOpt.get().status(), equalTo("DOWN"));

        Optional<CheckInfo> cetpServerCheckOpt = healthInfo.checks()
            .stream()
            .filter(check -> check.name().equals(CETP_SERVER_CHECK))
            .findFirst();
        assertTrue(cetpServerCheckOpt.isPresent());
        assertThat(cetpServerCheckOpt.get().status(), equalTo("UP"));
        assertThat(cetpServerCheckOpt.get().data().size(), equalTo(3));

        Optional<CheckInfo> statusCheckOpt = healthInfo.checks()
            .stream()
            .filter(check -> check.name().equals(STATUS_CHECK))
            .findFirst();
        assertTrue(statusCheckOpt.isPresent());
        assertThat(statusCheckOpt.get().status(), equalTo("DOWN"));

        Optional<CheckInfo> gitCheckOpt = healthInfo.checks()
            .stream()
            .filter(check -> check.name().equals(GIT_CHECK))
            .findFirst();
        assertTrue(gitCheckOpt.isPresent());
    }

    private void connectToCetpServer() throws Exception {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }}, new SecureRandom());
        SSLSocketFactory sslsocketfactory = sc.getSocketFactory();

        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket("localhost", CETPServer.DEFAULT_PORT);
        try {
            sslsocket.startHandshake();

            try (OutputStream outputstream = new BufferedOutputStream(sslsocket.getOutputStream());
                 FileInputStream in = new FileInputStream("src/test/resources/cetp/CARD_INSERTED.xml")) {
                byte[] event = in.readAllBytes();
                byte[] cetpHeader = new byte[]{'C', 'E', 'T', 'P'};
                outputstream.write(cetpHeader);
                new DataOutputStream(outputstream).writeInt(event.length);
                outputstream.write(event);
                outputstream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sslsocket.close();
        }

    }
}
