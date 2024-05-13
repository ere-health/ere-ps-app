package health.ere.ps.vau;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.io.DefaultHttpResponseParserFactory;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.SessionInputBufferImpl;
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
 * <p>
 * Certificate can be downloaded here:
 * https://fd.erezept-instanz1.titus.ti-dienste.de/VAUCertificate
 */
public class VAUEngine extends ApacheHttpClient43Engine {
    private static final Logger log = Logger.getLogger(VAUEngine.class.getName());
    private static final String responsePattern = "1 ([A-Fa-f0-9]{32}) (.*?)\r?\n\r?\n(.*)";
    private static final Pattern RESPONSE_PATTERN = Pattern.compile(responsePattern, Pattern.DOTALL);
    private final String fachdienstUrl;
    String requestid;
    String userpseudonym = "0";
    private VAU vau;
    private byte[] aeskey;

    public VAUEngine(String fachdienstUrl) {
        this.fachdienstUrl = fachdienstUrl;
    }

    /**
     * This function inits a Vau session described in the following document
     * https://fachportal.gematik.de/fachportal-import/files/gemSpec_Krypt_V2.19.0.pdf
     * Chapter 6 Page 78
     */
    public void initVauSession() {
        try {
            vau = new VAU(fachdienstUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected HttpEntity buildEntity(final ClientInvocation request) throws IOException {
        HttpEntity httpEntity = null;
        if(request.getMethod().equals("POST")) {
            httpEntity = super.buildEntity(request);
        }
        MultivaluedMap<String, Object> newHeaders = request.getHeaders().getHeaders();
        String userAgent = (String) newHeaders.getFirst("User-Agent");

        if (this.vau == null) {
            // init vauSession
            initVauSession();
        }

        String authorization = (String) newHeaders.getFirst("Authorization");

        if(authorization == null) {
            throw new IllegalStateException("Authorization header token must be given");
        }

        String accessCode = (String) newHeaders.getFirst("X-AccessCode");
        String contentType = (newHeaders.getFirst("Content-Type") != null) ? newHeaders.getFirst("Content-Type").toString() : "application/octet-stream";

        newHeaders.putSingle("X-erp-user", "l"); //Leistungserbringer
        newHeaders.putSingle("X-erp-resource", "Task");
        newHeaders.putSingle("Content-Type", "application/octet-stream");
        newHeaders.putSingle("Accept", "application/octet-stream");
        newHeaders.remove("Authorization");

        // request.getHeaders().setHeaders(newHeaders);

        byte[] finalMessageData;
        try {
            String content = request.getMethod() + " " + request.getUri().getPath() + (request.getUri().getQuery() != null ? "?"+request.getUri().getRawQuery() : "") +" HTTP/1.1\r\n" +
                "Host: " + request.getUri().getHost() + "\r\n" +
                "Authorization: " + authorization + "\r\n" +
                (accessCode != null ? "X-AccessCode: " + accessCode + "\r\n" : "") +
                "User-Agent: " + userAgent + "\r\n" +
                "Accept: application/fhir+xml; charset=utf-8\r\n";
            log.info(contentType);
            if(httpEntity != null) {
                    log.info(httpEntity.toString());
                    byte[] postBytes = httpEntity.getContent().readAllBytes();
                    String postBody = new String(postBytes);
                content += "Content-Type: " + contentType + "\r\n" +
                     "Content-Length: " + postBytes.length + "\r\n\r\n"
                        + postBody;
            } else {
                content += "\r\n";
            }

            String bearer = authorization.substring(7);
            requestid = VAU.byteArrayToHexString(vau.getRandom(16)).toLowerCase();
            aeskey = vau.getRandom(16);
            String aeskeyString = VAU.byteArrayToHexString(aeskey).toLowerCase();
            String p = "1 " + bearer + " " + requestid + " " + aeskeyString + " " + content;

            log.fine(p);

            finalMessageData = vau.encrypt(p);
        } catch (NoSuchAlgorithmException | IllegalStateException | InvalidCipherTextException | CertificateException
                | UnsupportedOperationException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        return EntityBuilder.create().setBinary(finalMessageData).setContentType(ContentType.create("application/octet-stream")).build();
    }

    @Override
    protected HttpRequestBase createHttpMethod(String url, String restVerb) {
        if ("GET".equals(restVerb)) {
            return new HttpPost(fachdienstUrl + "/VAU/" + userpseudonym);
        } else if ("POST".equals(restVerb)) {
            return new HttpPost(fachdienstUrl + "/VAU/" + userpseudonym);
        } else {
            final String verb = restVerb;
            return new HttpPost(url) {
                @Override
                public String getMethod() {
                    return verb;
                }
            };
        }
    }

    @Override
    public Response invoke(Invocation inv) {
        Response response = null;
        
        ClientInvocation request = (ClientInvocation) inv;
        if(request.getMethod().equals("GET")) {
            // enforce that build entity is called
            request.setEntityObject("");
        }
        response = super.invoke(inv);

        byte[] transportedData;
        byte[] responseBytes = null;
        String responseContent;
        try {
            String contentType = response.getHeaderString("Content-Type");
            if (!("application/octet-stream".equals(contentType))) {
                // A_20174
                throw new RuntimeException("VAU response content type has to be application/octet-stream but was: " + contentType + " Content: " + (response.getEntity() != null ? new String(((InputStream) response.getEntity()).readAllBytes()) : "null"));
            }
            responseBytes = ((InputStream) response.getEntity()).readAllBytes();
            log.fine(VAU.byteArrayToHexString(responseBytes));
            if(Response.Status.Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
                // if it is successful 
                transportedData = VAU.decryptWithKey(responseBytes, aeskey);
                if(!userpseudonym.equals(response.getHeaderString("userpseudonym")) && response.getHeaderString("userpseudonym") != null) {
                    userpseudonym = response.getHeaderString("userpseudonym");
                }
                responseContent = new String(transportedData);
                log.fine(responseContent);
                return parseResponseFromVAU(responseContent, (ClientInvocation) inv);
            } else {
                return response;
            }
        } catch (Exception e) {
            if(responseBytes != null) {
                log.info("VAU Response Bytes: "+VAU.byteArrayToHexString(responseBytes));
            }
            if(aeskey != null) {
                log.info("VAU AES Key: "+VAU.byteArrayToHexString(aeskey));
            }
            throw new RuntimeException(e);
        }
    }

    HttpResponse extractHttpResponse(String responseContent) throws IOException, HttpException {
        Matcher m = RESPONSE_PATTERN.matcher(responseContent);
        if (!m.matches()) {
            throw new RuntimeException("Response content does not match " + responsePattern + " was: " + responseContent);
        }

        String requestIdFromResponse = m.group(1);
        if (!requestIdFromResponse.equals(requestid)) {
            throw new RuntimeException("requestIdFromResponse (" + requestIdFromResponse + ") does not match requestid (" + requestid + ")");
        }
        String rawResponseHeader = m.group(2);
        String rawResponseBody = m.group(3);

        SessionInputBufferImpl buffer = new SessionInputBufferImpl(new HttpTransportMetricsImpl(), 8092);
        buffer.bind(new ByteArrayInputStream(rawResponseHeader.getBytes()));
        HttpResponse res = DefaultHttpResponseParserFactory.INSTANCE.create(buffer, MessageConstraints.DEFAULT).parse();
        res.setEntity(new StringEntity(rawResponseBody, ContentType.create("application/fhir+xml"/*res.getFirstHeader("Content-Type").getValue()*/)));
        return res;
    }

    private Response parseResponseFromVAU(String responseContent, ClientInvocation request) throws IOException, HttpException {
        HttpResponse res = extractHttpResponse(responseContent);

        ClientResponse response = new FinalizedClientResponse(request.getClientConfiguration(),
                request.getTracingLogger()) {
            InputStream stream;
            InputStream hc4Stream;

            public InputStream getInputStream() {
                if (stream == null) {
                    HttpEntity entity = res.getEntity();
                    if (entity == null)
                        return null;
                    try {
                        hc4Stream = entity.getContent();
                        stream = createBufferedStream(hc4Stream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return stream;
            }

            @Override
            protected void setInputStream(InputStream is) {
                stream = is;
                resetEntity();
            }

            @Override
            public void releaseConnection() throws IOException {
                releaseConnection(true);
            }

            @Override
            public void releaseConnection(boolean consumeInputStream) throws IOException {
                if (consumeInputStream) {
                    // Apache Client 4 is stupid,  You have to get the InputStream and close it if there is an entity
                    // otherwise the connection is never released.  There is, of course, no close() method on response
                    // to make this easier.
                    try {
                        // Another stupid thing...TCK is testing a specific exception from stream.close()
                        // so, we let it propagate up.
                        if (stream != null) {
                            stream.close();
                        } else {
                            InputStream is = getInputStream();
                            if (is != null) {
                                is.close();
                            }
                        }
                    } finally {
                        // just in case the input stream was entirely replaced and not wrapped, we need
                        // to close the apache client input stream.
                        if (hc4Stream != null) {
                            try {
                                hc4Stream.close();
                            } catch (IOException ignored) {
                            }
                        } else {
                            try {
                                HttpEntity entity = res.getEntity();
                                if (entity != null)
                                    entity.getContent().close();
                            } catch (IOException ignored) {
                            }
                        }
                    }
                } else if (res instanceof CloseableHttpResponse) {
                    try {
                        ((CloseableHttpResponse) res).close();
                    } catch (IOException e) {
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