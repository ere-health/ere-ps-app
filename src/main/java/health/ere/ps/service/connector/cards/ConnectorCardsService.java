package health.ere.ps.service.connector.cards;

import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservice.v8.Cards;
import de.gematik.ws.conn.cardservice.v8.PinStatusEnum;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.event.ChangePinEvent;
import health.ere.ps.event.ChangePinResponseEvent;
import health.ere.ps.event.GetPinStatusEvent;
import health.ere.ps.event.GetPinStatusResponseEvent;
import health.ere.ps.event.UnblockPinEvent;
import health.ere.ps.event.UnblockPinResponseEvent;
import health.ere.ps.event.VerifyPinEvent;
import health.ere.ps.event.VerifyPinResponseEvent;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.model.gematik.ChangePinResponse;
import health.ere.ps.model.gematik.GetPinStatusResponse;
import health.ere.ps.model.gematik.UnblockPinResponse;
import health.ere.ps.model.gematik.VerifyPinResponse;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.websocket.ExceptionWithReplyToException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.xml.ws.Holder;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
public class ConnectorCardsService {
    private static final Logger log = Logger.getLogger(ConnectorCardsService.class.getName());

    @Inject
    UserConfig userConfig;

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    @Inject
    Event<ChangePinResponseEvent> changePinResponseEvent;

    @Inject
    Event<VerifyPinResponseEvent> verifyPinResponseEvent;

    @Inject
    Event<UnblockPinResponseEvent> unblockPinResponseEvent;

    @Inject
    Event<GetPinStatusResponseEvent> getPinStatusResponseEvent;

    @Inject
    Event<Exception> exceptionEvent;


    private GetCardsResponse getConnectorCards(RuntimeConfig runtimeConfig) throws ConnectorCardsException {
        GetCards parameter = new GetCards();
        parameter.setContext(connectorServicesProvider.getContextType(runtimeConfig));
        try {
            return connectorServicesProvider.getEventServicePortType(runtimeConfig).getCards(parameter);
        } catch (FaultMessage e) {
            throw new ConnectorCardsException("Error getting connector card handles.", e);
        }
    }

    private Optional<List<CardInfoType>> getConnectorCardsInfo(RuntimeConfig runtimeConfig) throws ConnectorCardsException {
        GetCardsResponse response = getConnectorCards(runtimeConfig);
        List<CardInfoType> cardHandleTypeList = null;
        if (response != null) {
            Cards cards = response.getCards();
            cardHandleTypeList = cards.getCard();
            if (CollectionUtils.isEmpty(cardHandleTypeList)) {
                throw new ConnectorCardsException("Error. Did not receive and card handle data.");
            }
        }
        return Optional.ofNullable(cardHandleTypeList);
    }

    public String getConnectorCardHandle(CardHandleType cardHandleType) throws ConnectorCardsException {
        return getConnectorCardHandle(cardHandleType, null);
    }

    public String getConnectorCardHandle(
        CardHandleType cardHandleType,
        RuntimeConfig runtimeConfig
    ) throws ConnectorCardsException {
        Predicate<CardInfoType> predicate = ch -> ch.getCardType().value().equalsIgnoreCase(cardHandleType.getCardHandleType());
        return getConnectorCardHandle(predicate, runtimeConfig);
    }

    public String getConnectorCardHandle(String cardHolderName, RuntimeConfig runtimeConfig)
            throws ConnectorCardsException {
        return getConnectorCardHandle(ch ->
            cardHolderName.equals(ch.getCardHolderName()), runtimeConfig);
    }

    public String  getConnectorCardHandle(
        Predicate<? super CardInfoType> filter,
        RuntimeConfig runtimeConfig
    ) throws ConnectorCardsException {
        Optional<List<CardInfoType>> cardsInfoListOpt = getConnectorCardsInfo(runtimeConfig);
        String cardHandle = null;
        if (cardsInfoListOpt.isPresent()) {
            List<CardInfoType> typeList = cardsInfoListOpt.get();
            log.info(String.format("ConnectorCardsInfo: %s", print(typeList)));
            Optional<CardInfoType> cardHandleOpt = typeList.stream().filter(filter).findFirst();
            if (cardHandleOpt.isPresent()) {
                cardHandle = cardHandleOpt.get().getCardHandle();
            } else {
                throw new ConnectorCardsException("No card handle found for card.");
            }
        }
        return cardHandle;
    }

    public static String print(List<CardInfoType> typeList) {
        return typeList.stream().map(t -> String.format(
            "CardInfoType [cardHandle=%s, cardType=%s, ctId=%s, ICCSN=%s, slotId=%s, insertTime=%s]",
            t.getCardHandle(), t.getCardType(), t.getCtId(), t.getIccsn(), t.getSlotId(), t.getInsertTime()
        )).collect(Collectors.joining(", "));
    }


    public void onChangePinEvent(@ObservesAsync ChangePinEvent changePinEvent) {
        try {
            ChangePinResponse changePinResponse = changePin(changePinEvent.getCardHandle(), changePinEvent.getPinType(), changePinEvent.getRuntimeConfig());
            changePinResponseEvent.fireAsync(new ChangePinResponseEvent(changePinResponse, changePinEvent.getReplyTo(), changePinEvent.getId()));
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not change pin for card", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, changePinEvent.getReplyTo(), changePinEvent.getId()));
        }
    }
    
    public void onVerifyPinEvent(@ObservesAsync VerifyPinEvent verifyPinEvent) {
        try {
            VerifyPinResponse verifyPinResponse = verifyPin(verifyPinEvent.getCardHandle(), verifyPinEvent.getRuntimeConfig());
            verifyPinResponseEvent.fireAsync(new VerifyPinResponseEvent(verifyPinResponse, verifyPinEvent.getReplyTo(), verifyPinEvent.getId()));
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not verify pin for card", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, verifyPinEvent.getReplyTo(), verifyPinEvent.getId()));
        }
    }

    public void onUnblockPinEvent(@ObservesAsync UnblockPinEvent unblockPinEvent) {
        try {
            UnblockPinResponse unblockPinResponse = unblockPin(unblockPinEvent.getCardHandle(), unblockPinEvent.getPinType(), unblockPinEvent.getSetNewPin(), unblockPinEvent.getRuntimeConfig());
            unblockPinResponseEvent.fireAsync(new UnblockPinResponseEvent(unblockPinResponse, unblockPinEvent.getReplyTo(), unblockPinEvent.getId()));
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not unblock pin for card", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, unblockPinEvent.getReplyTo(), unblockPinEvent.getId()));
        }
    }

    public void onGetPinStatusEvent(@ObservesAsync GetPinStatusEvent getPinStatusEvent) {
        try {
            GetPinStatusResponse getPinStatusResponse = getPinStatus(getPinStatusEvent.getCardHandle(), getPinStatusEvent.getPinType(), getPinStatusEvent.getRuntimeConfig());
            getPinStatusResponseEvent.fireAsync(new GetPinStatusResponseEvent(getPinStatusResponse, getPinStatusEvent.getReplyTo(), getPinStatusEvent.getId()));
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not unblock pin for card", e);
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, getPinStatusEvent.getReplyTo(), getPinStatusEvent.getId()));
        }
    }

    /** 
     * @throws de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage
     */
    public ChangePinResponse changePin(String cardHandle, String pinType, RuntimeConfig runtimeConfig) throws de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage {
        Holder<Status> status = new Holder<>();
        Holder<PinResultEnum> pinResult = new Holder<>();
        Holder<BigInteger> leftTries = new Holder<>();
        connectorServicesProvider.getCardServicePortType(runtimeConfig).changePin(connectorServicesProvider.getContextType(runtimeConfig), cardHandle, pinType, status, pinResult, leftTries);
        return new ChangePinResponse(status.value, pinResult.value, leftTries.value);
    }

    /** 
     * @throws de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage
     */
    public VerifyPinResponse verifyPin(String cardHandle, RuntimeConfig runtimeConfig) throws de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage {
    	Holder<Status> status = new Holder<>();
        Holder<PinResultEnum> pinResultEnum = new Holder<>();
        Holder<BigInteger> leftTries = new Holder<>();
        connectorServicesProvider.getCardServicePortType(runtimeConfig).verifyPin(connectorServicesProvider.getContextType(runtimeConfig), cardHandle, "PIN.SMC", status, pinResultEnum, leftTries);
        return new VerifyPinResponse(status.value, pinResultEnum.value, leftTries.value);
    }

    /** 
     * @throws de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage
     */
    public UnblockPinResponse unblockPin(String cardHandle, String pinType, Boolean setNewPin, RuntimeConfig runtimeConfig) throws de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage {
    	Holder<Status> status = new Holder<>();
        Holder<PinResultEnum> pinResultEnum = new Holder<>();
        Holder<BigInteger> leftTries = new Holder<>();
        connectorServicesProvider.getCardServicePortType(runtimeConfig).unblockPin(connectorServicesProvider.getContextType(runtimeConfig), cardHandle, pinType, setNewPin, status, pinResultEnum, leftTries);
        return new UnblockPinResponse(status.value, pinResultEnum.value, leftTries.value);
    }

    /** 
     * @throws de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage
     */
    public GetPinStatusResponse getPinStatus(String cardHandle, String pinType, RuntimeConfig runtimeConfig) throws de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage {
    	Holder<Status> status = new Holder<>();
        Holder<PinStatusEnum> pinResultEnum = new Holder<>();
        Holder<BigInteger> leftTries = new Holder<>();
        connectorServicesProvider.getCardServicePortType(runtimeConfig).getPinStatus(connectorServicesProvider.getContextType(runtimeConfig), cardHandle, pinType, status, pinResultEnum, leftTries);
        return new GetPinStatusResponse(status.value, pinResultEnum.value, leftTries.value);
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
