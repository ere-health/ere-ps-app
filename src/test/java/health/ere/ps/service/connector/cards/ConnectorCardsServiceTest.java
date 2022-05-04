package health.ere.ps.service.connector.cards;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
class ConnectorCardsServiceTest {

    @Inject
    Logger logger;
    @Inject
    ConnectorCardsService connectorCardsService;


    @Test
    @Tag("titus") 
    void test_Successful_Retrieval_Of_SMC_B_Card_Handle() throws ConnectorCardsException {
        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);
        Assertions.assertTrue(StringUtils.isNotBlank(cardHandle), "Card handle result is present");

        logger.info("Card handle: " + cardHandle);
    }

    @Test
    @Tag("titus")
    void test_Successful_Retrieval_Of_eHBA_Card_Handle() throws ConnectorCardsException {
        String cardHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.HBA);
        Assertions.assertTrue(StringUtils.isNotBlank(cardHandle), "Card handle result is " +
                "present");

        logger.info("Card handle: " + cardHandle);
    }

    @Test
    @Tag("titus")
    void test_Unsuccessful_Retrieval_Of_Unsupported_KVK_Card_Handle() {
        Assertions.assertThrows(ConnectorCardsException.class,
                () -> {
                    connectorCardsService.getConnectorCardHandle(
                            ConnectorCardsService.CardHandleType.KVK);
                }, "ConnectorCardsException thrown for missing or unsupported card handle");
    }
}