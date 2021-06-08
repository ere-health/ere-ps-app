package health.ere.ps.resource.dgc;

import health.ere.ps.model.dgc.CertificateRequest;
import health.ere.ps.service.dgc.DigitalGreenCertificateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DigitalGreenCertificateResourceTest {
    @Mock
    private DigitalGreenCertificateService digitalGreenCertificateService;

    @InjectMocks
    private DigitalGreenCertificateResource digitalGreenCertificateResource;

    @Test
    void issue() {
        CertificateRequest certificateRequest = mock(CertificateRequest.class);

        byte[] bytes = new byte[]{123};

        given(digitalGreenCertificateService.issue(certificateRequest)).willReturn(bytes);

        Response response = digitalGreenCertificateResource.issue(certificateRequest);

        assertNotNull(response);
        assertEquals(response.getStatus(), 200);
        assertEquals(response.getMediaType().toString(), "application/pdf");
        assertSame(response.getEntity(), bytes);

        verifyNoInteractions(certificateRequest);
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

        given(digitalGreenCertificateService.issueVaccinationCertificate(fn, gn, dob, id1, tg1, vp1, mp1, ma1, dn1, sd1,
                dt1, id2, tg2, vp2, mp2, ma2, dn2, sd2, dt2)).willReturn(bytes);

        Response response = digitalGreenCertificateResource.issue(fn, gn, dob, id1, tg1, vp1, mp1, ma1, dn1, sd1, dt1,
                id2, tg2, vp2, mp2, ma2, dn2, sd2, dt2);

        assertNotNull(response);
        assertEquals(response.getStatus(), 200);
        assertEquals(response.getMediaType().toString(), "application/pdf");
        assertSame(response.getEntity(), bytes);
    }
}
