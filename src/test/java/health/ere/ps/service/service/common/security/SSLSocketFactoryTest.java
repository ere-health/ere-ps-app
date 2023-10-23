package health.ere.ps.service.common.security;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SSLSocketFactoryTest {

    private SSLSocketFactory underTest;
    private SSLSocketFactory delegateMock;

    @Test
    public void testCreateSocketWithStringAndInt() throws IOException, UnknownHostException {
        String host = "localhost";
        int port = 12345;
        Socket expectedSocket = new Socket();

        when(delegateMock.createSocket(host, port)).thenReturn(expectedSocket);

        Socket result = underTest.createSocket(host, port);

        assertSame(expectedSocket, result);
        verify(delegateMock).createSocket(host, port);
    }

    @Test
    public void testCreateSocketWithInetAddressAndInt() throws IOException {
        InetAddress address = InetAddress.getLocalHost();
        int port = 12345;
        Socket expectedSocket = new Socket();

        when(delegateMock.createSocket(address, port)).thenReturn(expectedSocket);

        Socket result = underTest.createSocket(address, port);

        assertSame(expectedSocket, result);
        verify(delegateMock).createSocket(address, port);
    }

    @Test
    public void testCreateSocketWithStringIntInetAddressAndInt() throws IOException, UnknownHostException {
        String host = "localhost";
        int port1 = 12345;
        InetAddress address = InetAddress.getLocalHost();
        int port2 = 6789;
        Socket expectedSocket = new Socket();

        when(delegateMock.createSocket(host, port1, address, port2)).thenReturn(expectedSocket);

        Socket result = underTest.createSocket(host, port1, address, port2);

        assertSame(expectedSocket, result);
        verify(delegateMock).createSocket(host, port1, address, port2);
    }

    @Test
    public void testCreateSocketWithInetAddressIntInetAddressAndInt() throws IOException {
        InetAddress address1 = InetAddress.getLocalHost();
        int port1 = 12345;
        InetAddress address2 = InetAddress.getLocalHost();
        int port2 = 6789;
        Socket expectedSocket = new Socket();

        when(delegateMock.createSocket(address1, port1, address2, port2)).thenReturn(expectedSocket);

        Socket result = underTest.createSocket(address1, port1, address2, port2);

        assertSame(expectedSocket, result);
        verify(delegateMock).createSocket(address1, port1, address2, port2);
    }

    @Test
    public void testCreateSocketWithSocketStringIntBoolean() throws IOException {
        Socket socket = new Socket();
        String host = "localhost";
        int port = 12345;
        boolean autoClose = true;
        Socket expectedSocket = new Socket();

        when(delegateMock.createSocket(socket, host, port, autoClose)).thenReturn(expectedSocket);

        Socket result = underTest.createSocket(socket, host, port, autoClose);

        assertSame(expectedSocket, result);
        verify(delegateMock).createSocket(socket, host, port, autoClose);
    }

    @Test
    public void testGetDefaultCipherSuites() {
        String[] expectedSuites = {"TLS_RSA_WITH_AES_128_CBC_SHA"};
        when(delegateMock.getDefaultCipherSuites()).thenReturn(expectedSuites);

        String[] result = underTest.getDefaultCipherSuites();

        assertArrayEquals(expectedSuites, result);
        verify(delegateMock).getDefaultCipherSuites();
    }

    @Test
    public void testGetSupportedCipherSuites() {
        String[] expectedSuites = {"TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"};
        when(delegateMock.getSupportedCipherSuites()).thenReturn(expectedSuites);

        String[] result = underTest.getSupportedCipherSuites();

        assertArrayEquals(expectedSuites, result);
        verify(delegateMock).getSupportedCipherSuites();
    }

    @Test
    public void testGetDefault() {
        assertSame(delegateMock, SSLSocketFactory.getDefault());
    }

    @Test
    public void testExceptionPropagation() throws IOException, UnknownHostException {
        String host = "invalid";
        int port = 12345;

        when(delegateMock.createSocket(host, port)).thenThrow(new UnknownHostException("Expected exception"));

        assertThrows(UnknownHostException.class, () -> {
            underTest.createSocket(host, port);
        });
    }
}
