package health.ere.ps.service.connector.cards;

import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservice.v8.Cards;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import health.ere.ps.exception.connector.ConnectorCardsException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
public class ConnectorCardsService {
    private static final Logger log = Logger.getLogger(ConnectorCardsService.class.getName());

    private final Map<String, String> hbaCardHandlesByPractitioner = new HashMap<>();

    @Inject
    EventServicePortType eventService;
    @Inject
    ContextType contextType;


    public String getSMCBConnectorCardHandle() throws ConnectorCardsException {
        List<CardInfoType> cardsInfoList = getConnectorCardsInfo();

        Optional<String> smcBCardHandle = cardsInfoList.stream()
                .filter(ch -> ch.getCardType().value().equalsIgnoreCase(CardHandleType.SMC_B.getCardHandleType()))
                .map(CardInfoType::getCardHandle)
                .findFirst();

        return smcBCardHandle.orElseThrow(() -> new ConnectorCardsException("No SMC_B card handle was found"));
    }

    public String getHBAConnectorCardHandle(String practitionerName) throws ConnectorCardsException {
        if (!hbaCardHandlesByPractitioner.containsKey(practitionerName)) {
            List<CardInfoType> hbaCardInfoTypes = getConnectorCardsInfo().stream()
                    .filter(ch -> CardHandleType.HBA.getCardHandleType().equalsIgnoreCase(ch.getCardType().value()))
                    .collect(Collectors.toList());

            //FOR PU TESTS, TODO:REMOVE ME BEFORE 19TH
            hbaCardInfoTypes.forEach(ch -> log.info("Hba card owner name found:" + ch.getCardHolderName()));

            Optional<String> practitionerHbaCardHandle = hbaCardInfoTypes.stream()
                    .filter(ch -> practitionerName.equalsIgnoreCase(ch.getCardHolderName()))
                    .map(CardInfoType::getCardHandle)
                    .findFirst();

            if (practitionerHbaCardHandle.isPresent()) {
                log.info("Hba card handle was found for practitioner:" + practitionerName);
                hbaCardHandlesByPractitioner.put(practitionerName, practitionerHbaCardHandle.get());
            } else {
                CardInfoType firstCardInfoType = hbaCardInfoTypes.stream()
                        .findFirst()
                        .orElseThrow(() -> new ConnectorCardsException("No HBA card handle was found"));

                log.warning("No hba card handle was found for practitioner:" + practitionerName
                        + ", returning the first one owned by:" + firstCardInfoType.getCardHolderName() + " instead");
                return firstCardInfoType.getCardHandle();
            }
        }
        return hbaCardHandlesByPractitioner.get(practitionerName);
    }

    private GetCardsResponse getConnectorCards() throws ConnectorCardsException {
        GetCards parameter = new GetCards();
        parameter.setContext(contextType);

        try {
            return eventService.getCards(parameter);
        } catch (FaultMessage e) {
            throw new ConnectorCardsException("Error getting connector card handles.", e);
        }
    }

    private List<CardInfoType> getConnectorCardsInfo() throws ConnectorCardsException {
        GetCardsResponse response = getConnectorCards();
        List<CardInfoType> cardHandleTypeList = new ArrayList<>();

        if (response != null) {
            Cards cards = response.getCards();
            cardHandleTypeList = cards.getCard();

            if (cardHandleTypeList.isEmpty()) {
                throw new ConnectorCardsException("Error. Did not receive and card handle data.");
            }
        }

        return cardHandleTypeList;
    }

    public enum CardHandleType {
        EGK("EGK"),
        HBA_Q_SIG("HBA-qSig"),
        HBA("HBA"),
        SMC_B("SMC-B"),
        HSM_B("HSM-B"),
        SMC_KT("SMC-KT"),
        KVK("KVK"),
        ZOD_2_0("ZOD_2.0"),
        UNKNOWN("UNKNOWN"),
        HBA_X("HBAx"),
        SM_B("SM-B");

        private final String cardHandleType;

        CardHandleType(String cardHandleType) {
            this.cardHandleType = cardHandleType;
        }

        public String getCardHandleType() {
            return cardHandleType;
        }
    }
}
