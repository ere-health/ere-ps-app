package health.ere.ps.service.cetp;

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import health.ere.ps.profile.RUDevTestProfile;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.xml.ws.Holder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@QuarkusTest
@TestProfile(RUDevTestProfile.class)
public class KonnektorSubscriptionTest {

    public static final String TEMP_CONFIG = "temp-config";
    private static String uuid;

    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);

    @Inject
    SubscriptionManager subscriptionManager;

    @Inject
    MultiConnectorServicesProvider multiConnectorServicesProvider;

    EventServicePortType eventService;

    @BeforeEach
    public void beforeEach() throws Exception {
        eventService = mock(EventServicePortType.class);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            final Object[] args = invocation.getArguments();

            Holder<Status> status = (Holder<Status>) args[2];
            Status subscribeStatus = new Status();
            subscribeStatus.setResult("Subscribed");
            status.value = subscribeStatus;

            Holder<String> subscriptionId = (Holder<String>) args[3];
            uuid = UUID.randomUUID().toString();
            subscriptionId.value = uuid;

            Holder<XMLGregorianCalendar> terminationTime = (Holder<XMLGregorianCalendar>) args[4];

            GregorianCalendar c = new GregorianCalendar();
            c.setTime(new Date());
            terminationTime.value = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

            return null;
        }).when(eventService).subscribe(any(), any(), any(), any(), any());

        Status status = new Status();
        status.setResult("Unsubscribed");
        when(eventService.unsubscribe(any(), any(), any())).thenReturn(status);

        MultiConnectorServicesProvider connectorServicesProvider = mock(MultiConnectorServicesProvider.class);
        when(connectorServicesProvider.getContextType(any())).thenReturn(new ContextType());
        when(connectorServicesProvider.getEventServicePortType(any())).thenReturn(eventService);

        QuarkusMock.installMockForType(connectorServicesProvider, MultiConnectorServicesProvider.class);

        new File(TEMP_CONFIG).delete();
    }

    @AfterEach
    public void afterAll() {
        QuarkusMock.installMockForType(multiConnectorServicesProvider, MultiConnectorServicesProvider.class);
    }

    @Test
    @Disabled
    public void defaultFolderConfigKonnektorSubscriptionReloadedInSync() throws Exception {
        subscriptionManager.setConfigFolder("config/konnektoren");
        int cnt = 4;
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
            futures.add(scheduledThreadPool.submit(() -> {
                Response response = given()
                    .queryParam("host", "192.168.178.42")
                    .when()
                    .get("/pharmacy/Subscribe");

                response.then().statusCode(200);
                List<String> responseBody = response.jsonPath().getList("$");
                assertThat(responseBody.size(), equalTo(1));
                return responseBody.get(0);
            }));
        }
        List<String> statuses = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
            statuses.add(futures.get(i).get());
        }
        assertThat(statuses.stream().anyMatch(s -> s.contains("later")), equalTo(true));
    }

    @Test
    public void fakeFolderConfigKonnektorSubscriptionReloadedWhenHostMatchesAppConfig() {
        File tempConfig = new File(TEMP_CONFIG);
        boolean ready = true;
        if (!tempConfig.exists()) {
            ready = tempConfig.mkdir();
        }
        if (ready) {
            subscriptionManager.setConfigFolder(tempConfig.getAbsolutePath());
            Response response = given()
                .queryParam("host", "192.168.178.42")
                .when()
                .get("/pharmacy/Subscribe");

            response.then().statusCode(200);
            List<String> responseBody = response.jsonPath().getList("$");
            assertThat(responseBody.size(), equalTo(1));
            assertThat(responseBody.get(0), containsString(String.format("Subscribed %s", uuid)));
        } else {
            fail("can't create tmp config folder");
        }
    }

    @Test
    public void fakeFolderConfigKonnektorSubscriptionNotReloadedWhenHostDoesntMatchAppConfig() {
        File tempConfig = new File(TEMP_CONFIG);
        boolean ready = true;
        if (!tempConfig.exists()) {
            ready = tempConfig.mkdir();
        }
        if (ready) {
            subscriptionManager.setConfigFolder(tempConfig.getAbsolutePath());
            String host = "192.168.178.52";
            Response response = given()
                .queryParam("host", host)
                .when()
                .get("/pharmacy/Subscribe");

            response.then().statusCode(200);
            List<String> responseBody = response.jsonPath().getList("$");
            assertThat(responseBody.size(), equalTo(1));
            String msg = String.format("No configuration is found for the given host: %s", host);
            assertThat(responseBody.get(0), equalTo(msg));
        } else {
            fail("can't create tmp config folder");
        }
    }

    @Test
    public void defaultFolderConfigKonnektorSubscriptionReloaded() {
        subscriptionManager.setConfigFolder("config/konnektoren");
        Response response = given()
            .queryParam("host", "192.168.178.42")
            .when()
            .get("/pharmacy/Subscribe");

        response.then().statusCode(200);
        List<String> responseBody = response.jsonPath().getList("$");
        assertThat(responseBody.size(), equalTo(1));
        assertThat(responseBody.get(0), containsString(String.format("Subscribed %s", uuid)));
    }

    @Test
    public void threeSubscriptionsReloaded() {
        subscriptionManager.setConfigFolder("config/konnektoren");
        Response response = given()
            .when()
            .get("/pharmacy/Subscribe");

        response.then().statusCode(200);
        List<String> responseBody = response.jsonPath().getList("$");
        assertThat(responseBody.size(), equalTo(3));
    }

    @Test
    public void subscriptionUnsubscribedByCetp() throws Exception {
        subscriptionManager.setConfigFolder("config/konnektoren");
        Response response = given()
            .when()
            .get("/pharmacy/Unsubscribe?useCetp=true");

        response.then().statusCode(200);
        List<String> responseBody = response.jsonPath().getList("$");
        assertThat(responseBody.size(), equalTo(3));

        ArgumentCaptor<String> subscriptionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> cetpCaptor = ArgumentCaptor.forClass(String.class);
        verify(eventService, times(3)).unsubscribe(any(), subscriptionIdCaptor.capture(), cetpCaptor.capture());
        assertTrue(subscriptionIdCaptor.getAllValues().stream().allMatch(Objects::isNull));
        assertTrue(cetpCaptor.getAllValues().stream().allMatch(s -> s.startsWith("cetp://")));

        File config8585 = new File("config/konnektoren/8585");
        assertThat(config8585.listFiles().length, equalTo(1));

        File config8586 = new File("config/konnektoren/8586");
        assertThat(config8586.listFiles().length, equalTo(1));

        File config8587 = new File("config/konnektoren/8587");
        assertThat(config8587.listFiles().length, equalTo(1));
    }
}
