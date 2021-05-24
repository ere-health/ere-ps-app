package health.ere.ps.vau;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
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
    private static Logger log = Logger.getLogger(VAUEngine.class.getName());
    private VAU vau;
    private String fachdienstUrl;
    private byte[] aeskey;
    private String userpseudonym = "0";

    public VAUEngine(String fachdienstUrl) {
        this.fachdienstUrl = fachdienstUrl;
    }

    /**
     * This function inits a Vau session described in the following document
     * https://fachportal.gematik.de/fachportal-import/files/gemSpec_Krypt_V2.19.0.pdf
     * Chapter 6 Page 78
     */
    public void initVauSession(String userAgent) {
        try {
            vau = new VAU(userAgent, fachdienstUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected HttpEntity buildEntity(final ClientInvocation request) throws IOException {
        HttpEntity httpEntity = super.buildEntity(request);
        MultivaluedMap<String, Object> newHeaders = request.getHeaders().getHeaders();
        String userAgent = (String) newHeaders.getFirst("User-Agent");
        if (this.vau == null) {
            // init vauSession
            initVauSession(userAgent);
        }
        String authorization = (String) newHeaders.getFirst("Authorization");
        String contentType = ((MediaType) newHeaders.getFirst("Content-Type")).toString();
        newHeaders.putSingle("X-erp-user", "l"); //Leistungserbringer
        newHeaders.putSingle("X-erp-resource", "Task");
        request.getHeaders().setHeaders(newHeaders);

        byte[] finalMessageData;
        try {
            byte[] postBytes = httpEntity.getContent().readAllBytes();
            String postBody = new String(postBytes);
            String content = request.getMethod()+" "+request.getUri().getPath()+" HTTP/1.1\n"+
            "Host: "+request.getUri().getHost()+"\n"+
            "Authorization: "+authorization+"\n"+
            "Content-Type: "+contentType+"\n"+
            "User-Agent: "+userAgent+"\n"+
            "Content-Length: "+postBytes.length+"\n"+
            "Accept: application/fhir+xml;charset=utf-8\n\n"
            +postBody;

            String bearer = authorization.substring(7);
            String requestid = VAU.ByteArrayToHexString(vau.GetRandom(16));
            aeskey = vau.GetRandom(16);
            String aeskeyString = VAU.ByteArrayToHexString(aeskey);
            String p = "1 "+bearer+" "+requestid.toLowerCase()+" "+aeskeyString.toLowerCase()+" "+content;

            log.info(p);

            finalMessageData = vau.encrypt(p);
        } catch (NoSuchAlgorithmException | IllegalStateException | InvalidCipherTextException | CertificateException
                | UnsupportedOperationException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }


        return EntityBuilder.create().setBinary(finalMessageData).setContentType(ContentType.create("application/octet-stream")).build();
    }

    @Override
    protected HttpRequestBase createHttpMethod(String url, String restVerb)
   {
      if ("GET".equals(restVerb))
      {
         return new HttpGet(url);
      }
      else if ("POST".equals(restVerb))
      {
         return new HttpPost(fachdienstUrl+"/VAU/"+userpseudonym);
      }
      else
      {
         final String verb = restVerb;
         return new HttpPost(url)
         {
            @Override
            public String getMethod()
            {
               return verb;
            }
         };
      }
   }

    @Override
    public Response invoke(Invocation inv) {
        Response response = super.invoke(inv);

        byte[] transportedData;
        try {
            byte[] responseBytes = ((InputStream) response.getEntity()).readAllBytes();
            log.info( VAU.ByteArrayToHexString(responseBytes));
            transportedData = vau.decryptWithKey(responseBytes, aeskey);
            userpseudonym = response.getHeaderString("userpseudonym");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Response.status(response.getStatus()).entity(new String(transportedData))
                .build();
    }

}
