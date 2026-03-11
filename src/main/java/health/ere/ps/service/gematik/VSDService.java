package health.ere.ps.service.gematik;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import de.gematik.ws.conn.vsds.vsdservice.v5.ReadVSDResponse;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.ws.Holder;

import java.util.UUID;

@ApplicationScoped
public class VSDService {

    @Inject
    MultiConnectorServicesProvider connectorServicesProvider;

    public ReadVSDResponse read(
        String egkHandle,
        String smcbHandle,
        RuntimeConfig runtimeConfig,
        boolean performOnlineCheck,
        boolean readOnlineReceipt
    ) throws FaultMessage {
        ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
        if ("".equals(context.getUserId()) || context.getUserId() == null) {
            context.setUserId(UUID.randomUUID().toString());
        }

        Holder<byte[]> persoenlicheVersichertendaten = new Holder<>();
        Holder<byte[]> allgemeineVersicherungsdaten = new Holder<>();
        Holder<byte[]> geschuetzteVersichertendaten = new Holder<>();
        Holder<VSDStatusType> vsdStatus = new Holder<>();
        Holder<byte[]> pruefungsnachweis = new Holder<>();

        VSDServicePortType vsdServicePortType = connectorServicesProvider.getVSDServicePortType(runtimeConfig);

        vsdServicePortType.readVSD(
            egkHandle, smcbHandle, performOnlineCheck, readOnlineReceipt, context,
            persoenlicheVersichertendaten,
            allgemeineVersicherungsdaten,
            geschuetzteVersichertendaten,
            vsdStatus,
            pruefungsnachweis
        );

        ReadVSDResponse readVSDResult = new ReadVSDResponse();
        readVSDResult.setPersoenlicheVersichertendaten(persoenlicheVersichertendaten.value);
        readVSDResult.setAllgemeineVersicherungsdaten(allgemeineVersicherungsdaten.value);
        readVSDResult.setGeschuetzteVersichertendaten(geschuetzteVersichertendaten.value);
        readVSDResult.setPruefungsnachweis(pruefungsnachweis.value);
        readVSDResult.setVSDStatus(vsdStatus.value);
        return readVSDResult;
    }
}
