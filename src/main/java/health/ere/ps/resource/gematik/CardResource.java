package health.ere.ps.resource.gematik;

import static health.ere.ps.resource.gematik.Extractors.extractRuntimeConfigFromHeaders;

import de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.model.gematik.ChangePinResponse;
import health.ere.ps.model.gematik.GetPinStatusResponse;
import health.ere.ps.model.gematik.UnblockPinResponse;
import health.ere.ps.model.gematik.VerifyPinResponse;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

@Path("/card")
public class CardResource {

    @Inject
    ConnectorCardsService connectorCardsService;

    @Inject
    UserConfig userConfig;

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @Path("/change-pin")
    public ChangePinResponse changePin(ChangePinParameter parameterObject)
            throws FaultMessage {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        return connectorCardsService.changePin(parameterObject.cardHandle, parameterObject.pinType, runtimeConfig);
    }

    @POST
    @Path("/verify-pin")
    public VerifyPinResponse verifyPin(String cardHandle) throws FaultMessage {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        return connectorCardsService.verifyPin(cardHandle, runtimeConfig);
    }

    @POST
    @Path("/unblock-pin")
    public UnblockPinResponse unblockPin(String cardHandle, String pinType, Boolean setNewPin) throws FaultMessage {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        return connectorCardsService.unblockPin(cardHandle, pinType, setNewPin, runtimeConfig);
    }

    @GET
    @Path("/pin-status")
    public GetPinStatusResponse getPinStatus(@QueryParam("cardHandle") String cardHandle, @QueryParam("pinType") String pinType)
            throws FaultMessage {
        RuntimeConfig runtimeConfig = extractRuntimeConfigFromHeaders(httpServletRequest, userConfig);
        return connectorCardsService.getPinStatus(cardHandle, pinType, runtimeConfig);
    }
}
