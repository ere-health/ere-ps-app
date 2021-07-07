package health.ere.ps.service.connector.cards;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.common.security.SecureSoapTransportConfigurer;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
class ConnectorCardsServiceTest {

    @Inject
    Logger logger;

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
    void test_Successful_Retrieval_Of_SMC_B_Card_Handle() throws ConnectorCardsException {
        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);
        Assertions.assertTrue(StringUtils.isNotBlank(cardHandle), "Card handle result is present");

        logger.info("Card handle: " + cardHandle);
    }

    @Test
    void test_Successful_Retrieval_Of_eHBA_Card_Handle() throws ConnectorCardsException {
        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.HBA);
        Assertions.assertTrue(StringUtils.isNotBlank(cardHandle), "Card handle result is " +
                "present");

        logger.info("Card handle: " + cardHandle);
    }

    @Test
    void test_Unsuccessful_Retrieval_Of_Unsupported_KVK_Card_Handle() {
        Assertions.assertThrows(ConnectorCardsException.class,
                () -> {
                    connectorCardsService.getConnectorCardHandle(
                            ConnectorCardsService.CardHandleType.KVK);
                }, "ConnectorCardsException thrown for missing or unsupported card handle");
    }
}