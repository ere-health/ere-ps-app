package health.ere.ps.service.connector.cards;

import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
class ConnectorCardsServiceTest {

    @Inject
    Logger logger;
    @Inject
    ConnectorCardsService connectorCardsService;


    @Test
    void test_Successful_Retrieval_Of_SMC_B_Card_Handle() throws ConnectorCardsException {
        String cardHandle = connectorCardsService.getSMCBConnectorCardHandle();
        Assertions.assertTrue(StringUtils.isNotBlank(cardHandle), "Card handle result is present");

        logger.info("SMC_B card handle: " + cardHandle);
    }

    @Test
    void test_Successful_Retrieval_Of_eHBA_Card_Handle() throws ConnectorCardsException {
        String cardHandle = connectorCardsService.getHBAConnectorCardHandle("test");
        Assertions.assertTrue(StringUtils.isNotBlank(cardHandle), "Card handle result is present");

        logger.info("Card handle: " + cardHandle);
    }
}