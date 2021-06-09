package health.ere.ps.vau;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.io.DefaultHttpResponseParserFactory;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.SessionInputBufferImpl;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.protocol.HttpContext;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.i18n.LogMessages;
import org.jboss.resteasy.client.jaxrs.i18n.Messages;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.FinalizedClientResponse;

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
    byte[] aeskey;
    String userpseudonym = "0";
    String requestid;

    private static final String responsePattern = "1 ([A-Fa-f0-9]{32}) (.*?)\r?\n\r?\n(.*)";
    private static final Pattern RESPONSE_PATTERN = Pattern.compile(responsePattern, Pattern.DOTALL);

    public VAUEngine(String fachdienstUrl) {
       /*super(HttpClients.custom()
         .setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {

         @Override
         public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            return 0;
         }
            
         })
       .build());*/
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
        String accessCode = (String) newHeaders.getFirst("X-AccessCode");
        String contentType = ((MediaType) newHeaders.getFirst("Content-Type")).toString();
        newHeaders.putSingle("X-erp-user", "l"); //Leistungserbringer
        newHeaders.putSingle("X-erp-resource", "Task");
        newHeaders.putSingle("Content-Type", "application/octet-stream");
        newHeaders.putSingle("Accept", "application/octet-stream");
        newHeaders.remove("Authorization");

        // request.getHeaders().setHeaders(newHeaders);

        byte[] finalMessageData;
        try {
            byte[] postBytes = httpEntity.getContent().readAllBytes();
            String postBody = new String(postBytes);
            String content = request.getMethod()+" "+request.getUri().getPath()+" HTTP/1.1\r\n"+
            "Host: "+request.getUri().getHost()+"\r\n"+
            "Authorization: "+authorization+"\r\n"+
            "Content-Type: "+contentType+"\r\n"+
            (accessCode != null ? "X-AccessCode: "+accessCode+"\r\n" : "")+
            "User-Agent: "+userAgent+"\r\n"+
            "Content-Length: "+postBytes.length+"\r\n"+
            "Accept: application/fhir+xml; charset=utf-8\r\n\r\n"
            +postBody;

            String bearer = authorization.substring(7);
            requestid = VAU.byteArrayToHexString(vau.getRandom(16)).toLowerCase();
            aeskey = vau.getRandom(16);
            String aeskeyString = VAU.byteArrayToHexString(aeskey).toLowerCase();
            String p = "1 "+bearer+" "+requestid+" "+aeskeyString+" "+content;

            log.fine(p);

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
        String responseContent;
        try {
           String contentType = response.getHeaderString("Content-Type");
           if(!("application/octet-stream".equals(contentType))) {
               // A_20174
               throw new RuntimeException("VAU response content type has to be application/octet-stream but was: "+contentType+" Content: "+(response.getEntity() != null ? new String(((InputStream) response.getEntity()).readAllBytes()) : "null"));
           }
            byte[] responseBytes = ((InputStream) response.getEntity()).readAllBytes();
            log.fine( VAU.byteArrayToHexString(responseBytes));
            transportedData = VAU.decryptWithKey(responseBytes, aeskey);
            // BUG: Titus does not support userpseudonym yet
            // userpseudonym = response.getHeaderString("userpseudonym");
            responseContent = new String(transportedData);
            log.fine(responseContent);
            return parseResponseFromVAU(responseContent, (ClientInvocation) inv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    HttpResponse extractHttpResponse(String responseContent) throws IOException, HttpException {
        Matcher m = RESPONSE_PATTERN.matcher(responseContent);
        if(!m.matches()) {
            throw new RuntimeException("Response content does not match "+responsePattern+" was: "+responseContent);
        }
        String requestIdFromResponse = m.group(1);
        if(!requestIdFromResponse.equals(requestid)) {
            throw new RuntimeException("requestIdFromResponse ("+requestIdFromResponse+") does not match requestid ("+requestid+")");
        }
        String rawResponseHeader = m.group(2);
        String rawResponseBody = m.group(3);
        
        SessionInputBufferImpl buffer = new SessionInputBufferImpl(new HttpTransportMetricsImpl(), 8092);
        buffer.bind(new ByteArrayInputStream(rawResponseHeader.getBytes()));
        HttpResponse res = DefaultHttpResponseParserFactory.INSTANCE.create(buffer,  MessageConstraints.DEFAULT).parse();
        res.setEntity(new StringEntity(rawResponseBody, ContentType.create("application/fhir+xml"/*res.getFirstHeader("Content-Type").getValue()*/)));
        return res;
    }
    
    Response parseResponseFromVAU(String responseContent, ClientInvocation request) throws IOException, HttpException {
        HttpResponse res = extractHttpResponse(responseContent);
        ClientResponse response = new FinalizedClientResponse(request.getClientConfiguration(), request.getTracingLogger())
        {
           InputStream stream;
  
           InputStream hc4Stream;
  
           @Override
           protected void setInputStream(InputStream is)
           {
              stream = is;
              resetEntity();
           }
  
           public InputStream getInputStream()
           {
              if (stream == null)
              {
                 HttpEntity entity = res.getEntity();
                 if (entity == null)
                    return null;
                 try
                 {
                    hc4Stream = entity.getContent();
                    stream = createBufferedStream(hc4Stream);
                 }
                 catch (IOException e)
                 {
                    throw new RuntimeException(e);
                 }
              }
              return stream;
           }
  
           @Override
           public void releaseConnection() throws IOException
           {
              releaseConnection(true);
           }
  
           @Override
           public void releaseConnection(boolean consumeInputStream) throws IOException
           {
              if (consumeInputStream)
              {
                 // Apache Client 4 is stupid,  You have to get the InputStream and close it if there is an entity
                 // otherwise the connection is never released.  There is, of course, no close() method on response
                 // to make this easier.
                 try
                 {
                    // Another stupid thing...TCK is testing a specific exception from stream.close()
                    // so, we let it propagate up.
                    if (stream != null)
                    {
                       stream.close();
                    }
                    else
                    {
                       InputStream is = getInputStream();
                       if (is != null)
                       {
                          is.close();
                       }
                    }
                 }
                 finally
                 {
                    // just in case the input stream was entirely replaced and not wrapped, we need
                    // to close the apache client input stream.
                    if (hc4Stream != null)
                    {
                       try
                       {
                          hc4Stream.close();
                       }
                       catch (IOException ignored)
                       {
  
                       }
                    }
                    else
                    {
                       try
                       {
                          HttpEntity entity = res.getEntity();
                          if (entity != null)
                             entity.getContent().close();
                       }
                       catch (IOException ignored)
                       {
                       }
  
                    }
  
                 }
              }
              else if (res instanceof CloseableHttpResponse)
              {
                 try
                 {
                    ((CloseableHttpResponse) res).close();
                 }
                 catch (IOException e)
                 {
                    LogMessages.LOGGER.warn(Messages.MESSAGES.couldNotCloseHttpResponse(), e);
                 }
              }
           }
  
        };
        response.setProperties(request.getMutableProperties());
        response.setStatus(res.getStatusLine().getStatusCode());
        response.setReasonPhrase(res.getStatusLine().getReasonPhrase());
        response.setHeaders(extractHeaders(res));
        response.setClientConfiguration(request.getClientConfiguration());
        return response;
    }

}
