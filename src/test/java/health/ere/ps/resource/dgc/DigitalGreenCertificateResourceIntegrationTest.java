package health.ere.ps.resource.dgc;

import health.ere.ps.model.dgc.*;
import health.ere.ps.service.dgc.DigitalGreenCertificateService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
class DigitalGreenCertificateResourceIntegrationTest {

    @InjectSpy
    private DigitalGreenCertificateService service;

    @TestHTTPEndpoint(DigitalGreenCertificateResource.class)
    @TestHTTPResource
    private URL url;

    @Test
    @Disabled("Old servlet version due ot avalon dependencies")
    /*
     * TODO fix servlet-api
     * Disabled because servlet is a old version
     */
    void issueVaccinationCertificate() throws Exception {

        // model copied from DigitalGreenCertificateServiceIntegrationTest
        String dob = "1921-01-01";
        String name = "Testname Lastname";
        String givenName = "Testgiven Name";
        String id = "testId";
        String tg = "testTg";
        String vp = "testVp";
        String mp = "testMp";
        String ma = "testMa";
        int dn = 123;
        int sd = 345;
        String dt = "2021-01-01";
        byte[] pdf = new byte[]{};

        final String requestBody = "{\"nam\":{" +
                "\"fn\": \"" + name + "\"," +
                "\"gn\": \"" + givenName + "\"" +
                "}," +
                "\"dob\": \"" + dob + "\"," +
                "\"v\": [{" +
                "\"id\": \"" + id + "\"," +
                "\"tg\": \"" + tg + "\"," +
                "\"vp\": \"" + vp + "\"," +
                "\"mp\": \"" + mp + "\"," +
                "\"ma\": \"" + ma + "\"," +
                "\"dn\": " + dn + "," +
                "\"sd\": " + sd + "," +
                "\"dt\": \"" + dt + "\"" +
                "}]}";


        final V v = new V();
        v.id = id;
        v.tg = tg;
        v.vp = vp;
        v.mp = mp;
        v.ma = ma;
        v.dn = dn;
        v.sd = sd;
        v.dt = dt;

        final PersonName personName = new PersonName();
        personName.gn = givenName;
        personName.fn = name;

        final VaccinationCertificateRequest vaccinationCertificateRequest = new VaccinationCertificateRequest();
        vaccinationCertificateRequest.nam = personName;
        vaccinationCertificateRequest.dob = dob;
        vaccinationCertificateRequest.v = Collections.singletonList(v);

        Client client = ClientBuilder.newBuilder().build();

        // mock response
        final ArgumentCaptor<CertificateRequest> ac = ArgumentCaptor.forClass(CertificateRequest.class);
        // doReturn because of the null check in issuePdf
        doReturn(pdf).when(service).issuePdf(ac.capture());

        Response response = client.target(url.toURI().resolve("v2/issue"))
                .request("application/pdf")
                .post(Entity.json(requestBody));

        // then
        assertEquals(200, response.getStatus());
        assertArrayEquals(pdf, response.readEntity(byte[].class));

        CertificateRequest value = ac.getValue();
        assertEquals(value, vaccinationCertificateRequest);

        // after
        client.close();
    }

    @Test
    @Disabled("Old servlet version due to avalon dependencies")
    /*
     * TODO fix servlet-api
     * Disabled because servlet is a old version
     */
    void issueRecoverCertificate() throws Exception {

        // model copied from DigitalGreenCertificateServiceIntegrationTest
        final String testId = "testId";
        final String testTg = "testTg";
        final String testIs = "testIs";
        final String testDateFr = "2023-01-01";
        final String testDateDu = "2022-01-01";
        final String testDateDf = "2021-01-01";
        final String testDataDob = "1921-01-01";
        final String firstName = "Testname Lastname";
        final String givenName = "Testgiven Name";

        final String requestBody = "{\"nam\":{" +
                "\"fn\": \"" + firstName + "\"," +
                "\"gn\": \"" + givenName + "\"" +
                "}," +
                "\"dob\": \"" + testDataDob + "\"," +
                "\"r\": [{" +
                "\"id\": \"" + testId + "\"," +
                "\"tg\": \"" + testTg + "\"," +
                "\"is\": \"" + testIs + "\"," +
                "\"fr\": \"" + testDateFr + "\"," +
                "\"du\": \"" + testDateDu + "\"," +
                "\"df\": \"" + testDateDf + "\""+
                "}]}";
        Client client = ClientBuilder.newBuilder().build();

        final PersonName testDataPersonName = new PersonName();
        testDataPersonName.fn = firstName;
        testDataPersonName.gn = givenName;
        final RecoveryEntry recoveryEntry = new RecoveryEntry();
        recoveryEntry.setId(testId);
        recoveryEntry.setTg(testTg);
        recoveryEntry.setIs(testIs);
        recoveryEntry.setFr(LocalDate.parse(testDateFr));
        recoveryEntry.setDu(LocalDate.parse(testDateDu));
        recoveryEntry.setDf(LocalDate.parse(testDateDf));

        final RecoveryCertificateRequest certificateRequest = new RecoveryCertificateRequest();
        certificateRequest.setNam(testDataPersonName);
        certificateRequest.dob(LocalDate.parse(testDataDob));
        certificateRequest.addRItem(recoveryEntry);

        byte[] pdf = new byte[]{};

        // mock response
        final ArgumentCaptor<CertificateRequest> ac = ArgumentCaptor.forClass(CertificateRequest.class);
        // doReturn because of the null check in issuePdf
        doReturn(pdf).when(service).issuePdf(ac.capture());

        Response response = client.target(url.toURI().resolve("v2/recovered"))
                .request("application/pdf")
                .post(Entity.json(requestBody));

        // then
        assertEquals(200, response.getStatus());
        assertArrayEquals(pdf, response.readEntity(byte[].class));

        CertificateRequest value = ac.getValue();
        assertEquals(value, certificateRequest);

        // after
        client.close();
    }
}
