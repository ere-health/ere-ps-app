package health.ere.ps.service.dgc;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
import health.ere.ps.model.dgc.CertificateRequest;
import health.ere.ps.model.dgc.PersonName;
import health.ere.ps.model.dgc.RecoveryCertificateRequest;
import health.ere.ps.model.dgc.RecoveryEntry;
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
                ma, dn, sd, dt));
    }

    @Test
    void issueRecoveryCertificatePdf() {
        String fn = "testFn";

        String gn = "testGn";

        LocalDate dob = LocalDate.of(2000, 1, 1);

        String id = "testId";

        String tg = "testTg";

        LocalDate fr = LocalDate.of(2021, 5, 31);

        String is = "testIs";

        LocalDate df = LocalDate.of(2021, 7, 1);

        LocalDate du = LocalDate.of(2022, 7, 1);

        byte[] bytes = new byte[]{56, 67, 78};

        RecoveryCertificateRequest recoveryCertificateRequest = new RecoveryCertificateRequest();

        recoveryCertificateRequest.setNam(new PersonName(fn, gn));
        recoveryCertificateRequest.setDob(dob);

        RecoveryEntry r = new RecoveryEntry();

        r.setId(id);
        r.setTg(tg);
        r.setFr(fr);
        r.setIs(is);
        r.setDf(df);
        r.setDu(du);

        recoveryCertificateRequest.setR(Collections.singletonList(r));

        doReturn(bytes).when(digitalGreenCertificateService).issuePdf(recoveryCertificateRequest);

        assertSame(bytes, digitalGreenCertificateService.issueRecoveryCertificatePdf(fn, gn, dob, id, tg, fr, is, df,
                du));
    }
}
