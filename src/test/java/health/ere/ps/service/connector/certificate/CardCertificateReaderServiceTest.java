package health.ere.ps.service.connector.certificate;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CardCertificateReaderServiceTest {
    @Inject
    CardCertificateReaderService cardCertificateReaderService;

    @ConfigProperty(name = "idp.client.id")
    String clientId;

    @ConfigProperty(name = "idp.connector.client.system.id")
    String clientSystem;

    @ConfigProperty(name = "idp.connector.workplace.id")
    String workplace;

    @ConfigProperty(name = "idp.connector.card.handle")
    String cardHandle;
    
    @Disabled("Disabled until Titus Idp Card Certificate Service API Endpoint Is Fixed By Gematik")
    @Test
    void testReadCardCertificate() throws ConnectorCardCertificateReadException {
        Assertions.assertTrue(ArrayUtils.isNotEmpty(
                cardCertificateReaderService.readCardCertificate(clientId, clientSystem,
                    workplace, cardHandle)), "Smart card certificate was retrieved");
    }
}