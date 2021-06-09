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

        String id1 = "testId1";

        String tg1 = "testTg1";

        String vp1 = "testVp1";

        String mp1 = "testMp1";

        String ma1 = "testMa1";

        Integer dn1 = 12;

        Integer sd1 = 34;

        String dt1 = "testDt1";

        String id2 = "testId2";

        String tg2 = "testTg2";

        String vp2 = "testVp2";

        String mp2 = "testMp2";

        String ma2 = "testMa2";

        Integer dn2 = 45;

        Integer sd2 = 56;

        String dt2 = "testDt2";

        byte[] bytes = new byte[]{123};

        when(digitalGreenCertificateService.issueVaccinationCertificatePdf(fn, gn, dob, id1, tg1, vp1, mp1, ma1, dn1, sd1,
                dt1, id2, tg2, vp2, mp2, ma2, dn2, sd2, dt2)).thenReturn(bytes);

        Response response = digitalGreenCertificateResource.issue(fn, gn, dob, id1, tg1, vp1, mp1, ma1, dn1, sd1, dt1,
                id2, tg2, vp2, mp2, ma2, dn2, sd2, dt2);

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
