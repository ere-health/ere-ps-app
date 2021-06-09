package health.ere.ps.service.dgc;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import health.ere.ps.LocalOfflineQuarkusTestProfile;
import health.ere.ps.model.dgc.*;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.model.idp.crypto.PkiKeyResolver;
import health.ere.ps.utils.dgc.TokendIntegrationTestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@QuarkusTest
@TestProfile(LocalOfflineQuarkusTestProfile.class)
@ExtendWith(PkiKeyResolver.class)
class DigitalGreenCertificateServiceIntegrationTest extends TokendIntegrationTestHelper {

    @Inject
    private DigitalGreenCertificateService digitalGreenCertificateService;

    @ConfigProperty(name = "digital-green-certificate-service.issuerAPIUrl")
    private String issuerApiUrl;

    private WireMockServer wireMockServer;

    private String mockPath;

    private MappingBuilder serverMatcher;
    private byte[] response;

    @BeforeEach
    void startup(@PkiKeyResolver.Filename("ecc") final PkiIdentity serverIdentity,
                 @PkiKeyResolver.Filename("C_CH_AUT_R2048") final PkiIdentity rsaClientIdentity) throws Exception {
        this.serverIdentity = serverIdentity;
        this.rsaClientIdentity = rsaClientIdentity;

        URL url = new URL(issuerApiUrl);

        if (!"localhost".equals(url.getHost())) {
            throw new RuntimeException("Testing is only possible for localhost urls");
        }
        mockPath = url.getPath();
        wireMockServer = new WireMockServer(wireMockConfig().port(url.getPort()).bindAddress("localhost"));
        wireMockServer.start();

        // mock setup for token
        String token = "testToken";
        mockTokenCreation(token);

        response = new byte[]{};
        serverMatcher = post(mockPath)
                .withHeader("Authorization", equalTo("Bearer " + token))
                .withHeader("Accept", equalTo("application/pdf"))
                .willReturn(ok().withBody(response));
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        wireMockServer.stop();
        // TODO wireMockServer need time to terminate, it exception.
        Thread.sleep(2000);
    }

    @Test
    void issueVaccinationCertificate() {

        // mock response

        LocalDate dob = LocalDate.of(1921, 1, 1);
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
        byte[] response = new byte[]{};

        wireMockServer.stubFor(serverMatcher
                .withHeader("Content-Type", equalTo("application/vnd.dgc.v1+json"))
                .withRequestBody(equalToJson("{\"nam\":{" +
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
                        "}]}"))
        );

        final V v = new V();
        v.id = id;
        v.tg = tg;
        v.vp = vp;
        v.mp = mp;
        v.ma = ma;
        v.dn = dn;
        v.sd = sd;
        v.dt = dt;

        final VaccinationCertificateRequest vaccinationCertificateRequest = new VaccinationCertificateRequest();
        vaccinationCertificateRequest.setNam(new PersonName(name, givenName));
        vaccinationCertificateRequest.setDob(dob);
        vaccinationCertificateRequest.v = Collections.singletonList(v);

        byte[] actualResponse = digitalGreenCertificateService.issuePdf(vaccinationCertificateRequest);
        assertNotNull(actualResponse);
        assertArrayEquals(response, actualResponse);
    }

    @Test
    void issueRecoveryCertificate() {
        // mock response

        final String testId = "testId";
        final String testTg = "testTg";
        final String testIs = "testIs";
        final String testDateFr = "2023-01-01";
        final String testDateDu = "2022-01-01";
        final String testDateDf = "2021-01-01";
        final String testDataDob = "1921-01-01";
        final String name = "Testname";
        final String givenName = "Testgiven Name";

        final String jsonContentResponse = "{\"nam\":{" +
                "\"fn\": \"" + name + "\"," +
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
        wireMockServer.stubFor(serverMatcher
                .withHeader("Content-Type", equalTo("application/vnd.dgc.v1+json"))
                .withRequestBody(equalToJson(jsonContentResponse))
                .withRequestBody(matchingJsonPath("r.length()", equalTo("1")))
        );

        final RecoveryEntry recoveryEntry = new RecoveryEntry();
        recoveryEntry.setId(testId);
        recoveryEntry.setTg(testTg);
        recoveryEntry.setIs(testIs);
        recoveryEntry.setFr(LocalDate.parse(testDateFr));
        recoveryEntry.setDu(LocalDate.parse(testDateDu));
        recoveryEntry.setDf(LocalDate.parse(testDateDf));

        final RecoveryCertificateRequest certificateRequest = new RecoveryCertificateRequest();
        certificateRequest.setNam(new PersonName(name, givenName));
        certificateRequest.setDob(LocalDate.parse(testDataDob));
        certificateRequest.addRItem(recoveryEntry);

        final byte[] actualResponse = digitalGreenCertificateService.issuePdf(certificateRequest);
        assertNotNull(actualResponse);
        assertArrayEquals(response, actualResponse);
    }

    @Test
    void issueRecoveryCertificateFromIndividualParams() {
        // mock response

        final String testId = "testId";
        final String testTg = "testTg";
        final String testIs = "testIs";
        final String testDateFr = "2023-01-01";
        final String testDateDu = "2022-01-01";
        final String testDateDf = "2021-01-01";
        final String testDataDob = "1921-01-01";
        final String name = "Testname";
        final String givenName = "Testgiven Name";

        final String jsonContentResponse = "{\"nam\":{" +
                "\"fn\": \"" + name + "\"," +
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
        wireMockServer.stubFor(serverMatcher
                .withHeader("Content-Type", equalTo("application/vnd.dgc.v1+json"))
                .withRequestBody(equalToJson(jsonContentResponse))
                .withRequestBody(matchingJsonPath("r.length()", equalTo("1")))
        );

        final byte[] actualResponse = digitalGreenCertificateService.issueRecoveryCertificatePdf(name, givenName,
                LocalDate.parse(testDataDob), testId, testTg, LocalDate.parse(testDateFr), testIs,
                LocalDate.parse(testDateDf), LocalDate.parse(testDateDu));
        assertNotNull(actualResponse);
        assertArrayEquals(response, actualResponse);
    }
}
