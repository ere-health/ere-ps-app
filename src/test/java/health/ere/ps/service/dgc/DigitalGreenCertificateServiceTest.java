package health.ere.ps.service.dgc;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
import health.ere.ps.model.dgc.CertificateRequest;
import health.ere.ps.model.dgc.PersonName;
import health.ere.ps.model.dgc.V;
import health.ere.ps.model.dgc.VaccinationCertificateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.enterprise.event.Event;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigitalGreenCertificateServiceTest {
    @Spy
    private DigitalGreenCertificateService digitalGreenCertificateService;

    @Test
    void issue() throws IOException {
        // mock token
        String token = "testToken";

        @SuppressWarnings("unchecked")
        Event<RequestBearerTokenFromIdpEvent> requestBearerTokenFromIdpEventEvent = mock(Event.class);

        doAnswer((invocation) -> {
            ((RequestBearerTokenFromIdpEvent) invocation.getArgument(0)).setBearerToken(token);
            return null;
        }).when(requestBearerTokenFromIdpEventEvent).fire(any());

        digitalGreenCertificateService.requestBearerTokenFromIdp = requestBearerTokenFromIdpEventEvent;

        // mock web request
        Client client = mock(Client.class);

        String issuerAPIUrl = "testIssuerAPIUrl";

        WebTarget webTarget = mock(WebTarget.class);

        Invocation.Builder builder1 = mock(Invocation.Builder.class);

        Invocation.Builder builder2 = mock(Invocation.Builder.class);

        CertificateRequest certificateRequest = mock(CertificateRequest.class);

        Response response = mock(Response.class);

        InputStream inputStream = mock(InputStream.class);

        byte[] bytes = new byte[]{34, 45, 56};

        digitalGreenCertificateService.issuerAPIUrl = issuerAPIUrl;
        digitalGreenCertificateService.client = client;
        when(client.target(issuerAPIUrl)).thenReturn(webTarget);
        when(webTarget.request("application/pdf")).thenReturn(builder1);
        when(builder1.header("Authorization", "Bearer " + token)).thenReturn(builder2);
        when(builder2.post(Entity.entity(certificateRequest, "application/vnd.dgc.v1+json"))).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(InputStream.class)).thenReturn(inputStream);
        when(inputStream.readAllBytes()).thenReturn(bytes);

        assertSame(bytes, digitalGreenCertificateService.issuePdf(certificateRequest));

        verifyNoInteractions(certificateRequest);
    }

    @Test
    void issueVaccinationCertificate() {
        String fn = "testName";

        String gn = "testGivenName";

        LocalDate dob = LocalDate.of(1967, 8, 9);

        String id = "testId";

        String tg = "testTg";

        String vp = "testVp";

        String mp = "testMp";

        String ma = "testMa";

        Integer dn = 1;

        Integer sd = 2;

        String dt = "testDt";

        VaccinationCertificateRequest vaccinationCertificateRequest = new VaccinationCertificateRequest();

        vaccinationCertificateRequest.setNam(new PersonName(fn, gn));
        vaccinationCertificateRequest.setDob(dob);

        V v = new V();

        v.id = id;
        v.tg = tg;
        v.vp = vp;
        v.mp = mp;
        v.ma = ma;
        v.dn = dn;
        v.sd = sd;
        v.dt = dt;

        vaccinationCertificateRequest.v = Collections.singletonList(v);

        byte[] response = new byte[]{123, 124, 125};

        doReturn(response).when(digitalGreenCertificateService).issuePdf(vaccinationCertificateRequest);

        assertEquals(response, digitalGreenCertificateService.issueVaccinationCertificatePdf(fn, gn, dob, id, tg, vp, mp,
                ma, dn, sd, dt, null, "some", "thing", "else", "with", 3, 4, "params"));
    }

    @Test
    void issueVaccinationCertificateWithSecondVaccination() {
        String fn = "testName";

        String gn = "testGivenName";

        LocalDate dob = LocalDate.of(1980, 12, 13);

        String id1 = "testId1";

        String tg1 = "testTg1";

        String vp1 = "testVp1";

        String mp1 = "testMp1";

        String ma1 = "testMa1";

        Integer dn1 = 1;

        Integer sd1 = 2;

        String dt1 = "testDt1";

        String id2 = "testId2";

        String tg2 = "testTg2";

        String vp2 = "testVp2";

        String mp2 = "testMp2";

        String ma2 = "testMa2";

        Integer dn2 = 3;

        Integer sd2 = 4;

        String dt2 = "testDt2";

        VaccinationCertificateRequest vaccinationCertificateRequest = new VaccinationCertificateRequest();

        vaccinationCertificateRequest.setNam(new PersonName(fn, gn));
        vaccinationCertificateRequest.setDob(dob);

        V v1 = new V();

        v1.id = id1;
        v1.tg = tg1;
        v1.vp = vp1;
        v1.mp = mp1;
        v1.ma = ma1;
        v1.dn = dn1;
        v1.sd = sd1;
        v1.dt = dt1;

        V v2 = new V();

        v2.id = id2;
        v2.tg = tg2;
        v2.vp = vp2;
        v2.mp = mp2;
        v2.ma = ma2;
        v2.dn = dn2;
        v2.sd = sd2;
        v2.dt = dt2;

        vaccinationCertificateRequest.v = List.of(v1, v2);

        byte[] response = new byte[]{123, 124, 125};

        doReturn(response).when(digitalGreenCertificateService).issuePdf(vaccinationCertificateRequest);

        assertEquals(response, digitalGreenCertificateService.issueVaccinationCertificatePdf(fn, gn, dob, id1, tg1, vp1,
                mp1, ma1, dn1, sd1, dt1, id2, tg2, vp2, mp2, ma2, dn2, sd2, dt2));
    }
}
