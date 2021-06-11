package health.ere.ps.service.connector.cards;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import javax.inject.Inject;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.common.security.SecureSoapTransportConfigurer;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ConnectorCardsServiceTest {

    @Inject
    ConnectorCardsService connectorCardsService;

    @Inject
    SecureSoapTransportConfigurer secureSoapTransportConfigurer;

    @Inject
    AppConfig appConfig;

    @BeforeEach
    void configureSecureTransport() throws SecretsManagerException {
        secureSoapTransportConfigurer.init(connectorCardsService);
        secureSoapTransportConfigurer.configureSecureTransport(
                appConfig.getEventServiceEndpointAddress(),
                SecretsManagerService.SslContextType.TLS,
                appConfig.getIdpConnectorTlsCertTrustStore(),
                appConfig.getIdpConnectorTlsCertTustStorePwd());
    }

    @Test
    void test_Successful_Retrieval_Of_SMC_B_Card_Handle() throws ConnectorCardsException, SecretsManagerException {
        Optional<String> cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);
        Assertions.assertTrue(cardHandle.isPresent(), "Card handle result is present");
        Assertions.assertTrue(cardHandle.get().equalsIgnoreCase(
                ConnectorCardsService.CardHandleType.SMC_B.getCardHandleType()));
    }

    @Test
    void test_Successful_Retrieval_Of_eHBA_Card_Handle() throws ConnectorCardsException, SecretsManagerException {
        Optional<String> cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.HBA);
        Assertions.assertTrue(cardHandle.isPresent(), "Card handle result is present");
        Assertions.assertTrue(cardHandle.get().equalsIgnoreCase(
                ConnectorCardsService.CardHandleType.HBA.getCardHandleType()));
    }
}