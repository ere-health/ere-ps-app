package health.ere.ps.service.cetp;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CETPServerTest {

    @Test
    @Disabled
    void testRun() throws UnknownHostException, IOException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {	return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        } }, new SecureRandom());
        SSLSocketFactory sslsocketfactory = sc.getSocketFactory();
         
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(
                        "localhost", 8585);

        sslsocket.startHandshake();

        try (OutputStream outputstream = new BufferedOutputStream(sslsocket.getOutputStream());
                FileInputStream in = new FileInputStream("src/test/resources/cetp/CARD_INSERTED.xml")) {
            byte[] event = in.readAllBytes();
            byte[] cetpHeader = new byte[] {'C', 'E', 'T', 'P'};
            outputstream.write(cetpHeader);
            new DataOutputStream(outputstream).writeInt(event.length);
            outputstream.write(event);
            outputstream.flush();
        }
        sslsocket.close();
        
    }
}
