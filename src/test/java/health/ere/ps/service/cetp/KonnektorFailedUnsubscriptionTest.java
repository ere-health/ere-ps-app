package health.ere.ps.service.cetp;

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.gematik.ws.tel.error.v2.Error;
import de.health.service.cetp.SubscriptionManager;
import de.health.service.cetp.konnektorconfig.FSConfigService;
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
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import static de.health.service.cetp.utils.Utils.deleteFiles;
import static de.health.service.cetp.utils.Utils.writeFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@QuarkusTest
@Disabled
@TestProfile(RUDevTestProfile.class)
public class KonnektorFailedUnsubscriptionTest {

    public static final String TEMP_CONFIG = "temp-config";
    private static String uuid;

    @Inject
    SubscriptionManager subscriptionManager;

    @Inject
    MultiConnectorServicesProvider multiConnectorServicesProvider;

    @BeforeEach
    public void beforeEach() throws Exception {
        EventServicePortType eventService = mock(EventServicePortType.class);
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


        Error faultInfo = new Error();
        faultInfo.setMessageID("MessageID");
        Mockito.doThrow(new FaultMessage("Syntaxfehler", faultInfo)).when(eventService).unsubscribe(any(), any(), any());

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
    public void subscriptionWasReloadedAndFileIsCreated() throws Exception {
        File config8585 = new File("config/konnektoren/8585");
        deleteFiles(config8585, file -> !file.getName().endsWith(FSConfigService.PROPERTIES_EXT));

        subscriptionManager.onStart(null);
        subscribeSucceeded(config8585);
    }

    @Test
    public void subscriptionWasNotReloadedDueToUnsubscribeError() throws Exception {
        File config8585 = new File("config/konnektoren/8585");
        writeFile(config8585.getAbsolutePath() + "/" + UUID.randomUUID(), null);

        subscriptionManager.onStart(null);
        subscribeFailed(config8585);
        subscribeSucceeded(config8585);
    }

    private void subscribeSucceeded(File config8585) {
        Response response = given()
            .queryParam("host", "192.168.178.42")
            .when()
            .get("/pharmacy/Subscribe");

        response.then().statusCode(200);
        List<String> responseBody = response.jsonPath().getList("$");
        assertThat(responseBody.size(), equalTo(1));
        assertThat(responseBody.get(0), containsString("Subscribed"));

        File[] files = config8585.listFiles((dir, name) -> !name.endsWith(".properties"));
        assertThat(files.length, equalTo(1));
        assertThat(files[0].getName().length(), equalTo(36));
    }

    private void subscribeFailed(File config8585) {
        Response response = given()
            .queryParam("host", "192.168.178.42")
            .when()
            .get("/pharmacy/Subscribe");

        response.then().statusCode(200);
        List<String> responseBody = response.jsonPath().getList("$");
        assertThat(responseBody.size(), equalTo(1));
        assertThat(responseBody.get(0), equalTo("Syntaxfehler"));

        File[] files = config8585.listFiles((dir, name) -> !name.endsWith(".properties"));
        assertThat(files.length, equalTo(1));
        assertThat(files[0].getName(), containsString("failed-unsubscription"));
    }
}
