package health.ere.ps.service.dgc;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
import health.ere.ps.exception.dgc.DgcCertificateServiceAuthenticationException;
import health.ere.ps.exception.dgc.DgcCertificateServiceException;
import health.ere.ps.exception.dgc.DgcException;
import health.ere.ps.exception.dgc.DgcInvalidParametersException;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void issuePdf() throws IOException {
        InputStream inputStream = mock(InputStream.class);

        CertificateRequest certificateRequest = mock(CertificateRequest.class);

        byte[] bytes = new byte[]{34, 45, 56};

        Response response = mockAuthenticatedResponse(certificateRequest);

        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(InputStream.class)).thenReturn(inputStream);
        when(inputStream.readAllBytes()).thenReturn(bytes);

        assertSame(bytes, digitalGreenCertificateService.issuePdf(certificateRequest));

        verifyNoInteractions(certificateRequest);
    }

    @Test
    void issuePdfWithCertificateServiceException() {
        issuePdfWithCertificateServiceException(401, DgcCertificateServiceAuthenticationException.class, 100401);
        issuePdfWithCertificateServiceException(403, DgcCertificateServiceAuthenticationException.class, 100403);
        issuePdfWithCertificateServiceException(400, DgcInvalidParametersException.class, 100400);
        issuePdfWithCertificateServiceException(406, DgcInvalidParametersException.class, 100406);
        issuePdfWithCertificateServiceException(500, DgcCertificateServiceException.class, 100500);
        // other unknown codes
        issuePdfWithCertificateServiceException(543, DgcCertificateServiceException.class, 100543);
    }


    @Test
    void issueVaccinationCertificate() {
        String fn = "testName";

        String gn = "testGivenName";

        String dob = "testDob";

        String id = "testId";

        String tg = "testTg";

        String vp = "testVp";

        String mp = "testMp";

        String ma = "testMa";

        Integer dn = 1;

        Integer sd = 2;

        String dt = "testDt";

        VaccinationCertificateRequest vaccinationCertificateRequest = new VaccinationCertificateRequest();

        PersonName nam = new PersonName();

        nam.fn = fn;
        nam.gn = gn;

        vaccinationCertificateRequest.nam = nam;
        vaccinationCertificateRequest.dob = dob;

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

    private void issuePdfWithCertificateServiceException(int responseCode, Class<? extends DgcException> expectedExceptionClass,
                                                         int expectedErrorCode) {

        CertificateRequest certificateRequest = mock(CertificateRequest.class);

        Response response = mockAuthenticatedResponse(certificateRequest);

        when(response.getStatus()).thenReturn(responseCode);

        DgcException dgcException = assertThrows(expectedExceptionClass
                ,() -> digitalGreenCertificateService.issuePdf(certificateRequest));

        assertEquals(expectedErrorCode, dgcException.getCode());
    }

    private Response mockAuthenticatedResponse(CertificateRequest certificateRequest) {
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

        Response response = mock(Response.class);

        digitalGreenCertificateService.issuerAPIUrl = issuerAPIUrl;
        digitalGreenCertificateService.client = client;
        when(client.target(issuerAPIUrl)).thenReturn(webTarget);
        when(webTarget.request("application/pdf")).thenReturn(builder1);
        when(builder1.header("Authorization", "Bearer " + token)).thenReturn(builder2);
        when(builder2.post(Entity.entity(certificateRequest, "application/vnd.dgc.v1+json"))).thenReturn(response);

        return response;
    }
}
