package health.ere.ps.resource.dgc;

import health.ere.ps.model.dgc.CertificateRequest;
import health.ere.ps.model.dgc.RecoveryCertificateRequest;
import health.ere.ps.model.dgc.VaccinationCertificateRequest;
import health.ere.ps.service.dgc.DigitalGreenCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalGreenCertificateResourceTest {
    @Spy
    private DigitalGreenCertificateService digitalGreenCertificateService;

    @InjectMocks
    private DigitalGreenCertificateResource digitalGreenCertificateResource;

    final byte[] bytes = new byte[]{123};

    @BeforeEach
    public void init() {
        // start with doReturn because null check.
        doReturn(bytes).when(digitalGreenCertificateService).issuePdf(Mockito.any(CertificateRequest.class));
    }

    @Test
    void issueVaccinationCertificate() {
        VaccinationCertificateRequest vaccinationCertificateRequest = mock(VaccinationCertificateRequest.class);

        Response response = digitalGreenCertificateResource.issue(vaccinationCertificateRequest);

        verify(digitalGreenCertificateService, times(1)).issuePdf(vaccinationCertificateRequest);
        assertNotNull(response);
        assertEquals(response.getStatus(), 200);
        assertEquals(response.getMediaType().toString(), "application/pdf");
        assertSame(response.getEntity(), bytes);

        verifyNoInteractions(vaccinationCertificateRequest);
    }

    @Test
    void issueVaccinationCertificateFromParameters() {
        String fn = "testFn";

        String gn = "testGn";

        String dob = "testDob";

        String id = "testId";

        String tg = "testTg";

        String vp = "testVp";

        String mp = "testMp";

        String ma = "testMa";

        Integer dn = 12;

        Integer sd = 34;

        String dt = "testDt";

        byte[] bytes = new byte[]{123};

        when(digitalGreenCertificateService.issueVaccinationCertificatePdf(fn, gn, dob, id, tg, vp, mp, ma, dn, sd, dt))
                .thenReturn(bytes);

        Response response = digitalGreenCertificateResource.issue(fn, gn, dob, id, tg, vp, mp, ma, dn, sd, dt);

        assertNotNull(response);
        assertEquals(response.getStatus(), 200);
        assertEquals(response.getMediaType().toString(), "application/pdf");
        assertSame(response.getEntity(), bytes);
    }

    @Test
    void issueRecoveryCertificate() {
        RecoveryCertificateRequest certificateRequest = mock(RecoveryCertificateRequest.class);

        Response response = digitalGreenCertificateResource.recovered(certificateRequest);

        verify(digitalGreenCertificateService, times(1)).issuePdf(certificateRequest);

        assertNotNull(response);
        assertEquals(response.getStatus(), 200);
        assertEquals(response.getMediaType().toString(), "application/pdf");
        assertSame(response.getEntity(), bytes);

        verifyNoInteractions(certificateRequest);
    }
}
