package health.ere.ps.vau;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;

/**
 * Engine for RestEasy inspired by the Gematik implementation of VAU:
 * https://github.com/gematik/ref-ePA-vauchannel/blob/master/vauchannel-cxf/src/main/java/de/gematik/ti/vauchannel/cxf/AESInterceptor.java
 * 
 * Certificate can be downloaded here:
 * https://fd.erezept-instanz1.titus.ti-dienste.de/VAUCertificate
 */
public class VAUEngine extends ApacheHttpClient43Engine {
    private VAU vau;
    private String vauURL; 

    public VAUEngine(String vauURL) {
        this.vauURL = vauURL;
    }

    /**
     * This function inits a Vau session described in the following document
     * https://fachportal.gematik.de/fachportal-import/files/gemSpec_Krypt_V2.19.0.pdf
     * Chapter 6 Page 78
     */
    public void initVauSession(String userAgent) {
        try {
            vau = new VAU(userAgent, vauURL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected HttpEntity buildEntity(final ClientInvocation request) throws IOException {
        MultivaluedMap<String, Object> newHeaders = request.getHeaders().getHeaders();
        if (this.vau == null) {
            // init vauSession
            initVauSession((String) newHeaders.getFirst("User-Agent"));
        }

        newHeaders.putSingle("X-erp-user", "l"); //Leistungserbringer
        newHeaders.putSingle("X-erp-resource", "Task");
        request.getHeaders().setHeaders(newHeaders);

        HttpEntity httpEntity = super.buildEntity(request);

        byte[] finalMessageData;
        try {
            finalMessageData = vau.encrypt(new String(httpEntity.getContent().readAllBytes()));
        } catch (NoSuchAlgorithmException | IllegalStateException | InvalidCipherTextException | CertificateException
                | UnsupportedOperationException e) {
            throw new RuntimeException(e);
        }

        return EntityBuilder.create().setBinary(finalMessageData).setContentType(ContentType.create("application","octet-stream")).build();
    }

    @Override
    public Response invoke(Invocation inv) {
        Response response = super.invoke(inv);

        byte[] transportedData;
        try {
            transportedData = vau.decrypt(response.getEntity().toString().getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Response.status(response.getStatus()).entity(new String(transportedData))
                .build();
    }

}
