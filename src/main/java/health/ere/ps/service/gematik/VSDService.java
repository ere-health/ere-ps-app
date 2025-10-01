package health.ere.ps.service.gematik;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.ReadVSDResponse;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import de.gematik.ws.tel.error.v2.Error;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.ws.Holder;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.SaxonXMLGregorianCalendar;

import java.util.Calendar;
import java.util.UUID;

import static de.gematik.ws.conn.cardservicecommon.v2.CardTypeType.EGK;
import static de.gematik.ws.conn.cardservicecommon.v2.CardTypeType.SMC_B;
import static health.ere.ps.service.gematik.PrefillPrescriptionService.getFirstCardOfType;
import static java.util.Locale.GERMANY;

@ApplicationScoped
public class VSDService {

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    public ReadVSDResponse readVSD(
        RuntimeConfig runtimeConfig,
        String egkHandleParameter,
        String smcbHandleParameter,
        boolean performOnlineCheck,
        boolean readOnlineReceipt
    ) throws FaultMessage, de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);
        String egkHandle = egkHandleParameter != null ? egkHandleParameter : getFirstCardOfType(eventService, EGK, context);

        if (egkHandle == null) {
            Error faultInfo = new Error();
            faultInfo.setMessageID(UUID.randomUUID().toString());
            DateTimeValue dateTimeValue = new DateTimeValue(Calendar.getInstance(GERMANY), true);
            faultInfo.setTimestamp(new SaxonXMLGregorianCalendar(dateTimeValue));
            throw new FaultMessage("EGK card is not found", faultInfo);
        }

        String smcbHandle = smcbHandleParameter != null
            ? smcbHandleParameter
            : (runtimeConfig != null && runtimeConfig.getSMCBHandle() != null)
                ? runtimeConfig.getSMCBHandle()
                : getFirstCardOfType(eventService, SMC_B, context);

        if (runtimeConfig != null) {
            runtimeConfig.setSMCBHandle(smcbHandle);
        }

        Holder<byte[]> persoenlicheVersichertendaten = new Holder<>();
        Holder<byte[]> allgemeineVersicherungsdaten = new Holder<>();
        Holder<byte[]> geschuetzteVersichertendaten = new Holder<>();
        Holder<VSDStatusType> vsdStatus = new Holder<>();
        Holder<byte[]> pruefungsnachweis = new Holder<>();

        connectorServicesProvider.getVSDServicePortType(runtimeConfig).readVSD(
            egkHandle,
            smcbHandle,
            performOnlineCheck,
            readOnlineReceipt,
            context,
            persoenlicheVersichertendaten,
            allgemeineVersicherungsdaten,
            geschuetzteVersichertendaten,
            vsdStatus,
            pruefungsnachweis
        );

        ReadVSDResponse readVSDResponse = new ReadVSDResponse();
        readVSDResponse.setVSDStatus(vsdStatus.value);
        readVSDResponse.setAllgemeineVersicherungsdaten(allgemeineVersicherungsdaten.value);
        readVSDResponse.setGeschuetzteVersichertendaten(geschuetzteVersichertendaten.value);
        readVSDResponse.setPersoenlicheVersichertendaten(persoenlicheVersichertendaten.value);
        readVSDResponse.setPruefungsnachweis(pruefungsnachweis.value);
        return readVSDResponse;
    }
}