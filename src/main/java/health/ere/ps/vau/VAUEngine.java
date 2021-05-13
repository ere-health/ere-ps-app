package health.ere.ps.vau;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;

import de.gematik.ti.vauchannel.protocol.TransportedData;
import de.gematik.ti.vauchannel.protocol.VAUProtocol;
import de.gematik.ti.vauchannel.protocol.VAUProtocolSession;
import de.gematik.ti.vauchannel.protocol.helpers.VAUProtocolCryptoImpl;

/**
 * Engine for RestEasy inspired by the Gematik implementation of VAU:
 * https://github.com/gematik/ref-ePA-vauchannel/blob/master/vauchannel-cxf/src/main/java/de/gematik/ti/vauchannel/cxf/AESInterceptor.java
 */
public class VAUEngine extends ApacheHttpClient43Engine {
    private VAUProtocol vauProtocol;
    private URI vauHandshakeUri;

    public VAUEngine(URI vauHandshakeUri) {
        this.vauHandshakeUri = vauHandshakeUri;
    }

    /**
     * This function inits a Vau session described in the following document
     * https://fachportal.gematik.de/fachportal-import/files/gemSpec_Krypt_V2.19.0.pdf
     * Chapter 6 Page 78
     */
    public void initVauSession() {
        try {
            vauProtocol = new VAUProtocol(new VAUProtocolCryptoImpl(true), new VAUProtocolSession(true));
            byte[] authzToken = null;
            String vAUClientHello = vauProtocol.handshakeStep1_generate_VAUClientHello_Message(authzToken);
            HttpPost httpPost = new HttpPost(vauHandshakeUri);
            httpPost.setEntity(EntityBuilder.create().setText(vAUClientHello)
                    .setContentType(ContentType.APPLICATION_JSON).build());
            String vAUServerHello;
            vAUServerHello = new String(httpClient.execute(httpPost).getEntity().getContent().readAllBytes());
            String vAUClientSigFin = vauProtocol.handshakeStep3_generate_VAUClientSigFin_Message(vAUServerHello);
            HttpPost httpPost2 = new HttpPost(vauHandshakeUri);
            httpPost2.setEntity(EntityBuilder.create().setText(vAUClientSigFin)
                    .setContentType(ContentType.APPLICATION_JSON).build());
            String vAUServerFin = new String(httpClient.execute(httpPost).getEntity().getContent().readAllBytes());
            vauProtocol.handshakeStep5_validate_VAUServerFin_Message(vAUServerFin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected HttpEntity buildEntity(final ClientInvocation request) throws IOException {
        if (this.vauProtocol == null) {
            // init vauSession
            initVauSession();
        }
        HttpEntity httpEntity = super.buildEntity(request);

        TransportedData transportedData = new TransportedData(httpEntity.getContent().readAllBytes(),
                httpEntity.getContentType().getValue());
        byte[] finalMessageData = vauProtocol.encrypt(transportedData);

        return EntityBuilder.create().setBinary(finalMessageData).setContentType(ContentType.APPLICATION_JSON).build();
    }

    @Override
    public Response invoke(Invocation inv) {
        Response response = super.invoke(inv);

        TransportedData transportedData = vauProtocol.decrypt(response.getEntity().toString().getBytes());

        return Response.status(response.getStatus()).entity(transportedData.body).type(transportedData.contentType)
                .build();
    }

}
