package health.ere.ps.service.cetp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.*;
import java.util.Arrays;
import java.util.List;

public class LocalAddressInSameSubnetFinderTest {

    @Test
    public void subnetMatch() throws UnknownHostException {
        LocalAddressInSameSubnetFinder ipFinder = new LocalAddressInSameSubnetFinder();
        Assertions.assertTrue(ipFinder.isInSubnet(
            createInterfaceAddress(
                InetAddress.getByName("192.168.178.15"),
                InetAddress.getByName("192.168.178.255"),
                (short) 24
            ),
            InetAddress.getByName("192.168.178.200")
        ));
    }

    @Test
    public void subnetSmallMaskNoMatch() throws UnknownHostException {
        LocalAddressInSameSubnetFinder ipFinder = new LocalAddressInSameSubnetFinder();
        Assertions.assertFalse(ipFinder.isInSubnet(
            createInterfaceAddress(
                InetAddress.getByName("192.168.178.14"),
                InetAddress.getByName("192.168.178.15"),
                (short) 30
            ),
            InetAddress.getByName("192.168.178.200")
        ));
    }

    @Test
    public void allSubnetMatch() throws UnknownHostException {
        LocalAddressInSameSubnetFinder ipFinder = new LocalAddressInSameSubnetFinder();
        Assertions.assertTrue(ipFinder.isInSubnet(
            createInterfaceAddress(
                InetAddress.getByName("192.168.178.15"),
                InetAddress.getByName("255.255.255.255"),
                (short) 0
            ),
            InetAddress.getByName("10.10.13.15")
        ));
    }

    @Test
    public void localIPskipLoopback() throws Exception {
        LocalAddressInSameSubnetFinder ipFinder = new LocalAddressInSameSubnetFinder();
        Inet4Address result = ipFinder.findHostAddress(
            List.of(
                createNetworkInterface(
                    true, true,
                    createInterfaceAddress(
                        InetAddress.getByName("192.168.178.10"),
                        InetAddress.getByName("192.168.178.255"),
                        (short) 24
                    )
                ),
                createNetworkInterface(
                    false, true,
                    createInterfaceAddress(
                        InetAddress.getByName("192.168.10.10"),
                        InetAddress.getByName("192.168.255.255"),
                        (short) 16
                    )
                )
            ),
            (Inet4Address) InetAddress.getByName("192.168.10.15")
        );

        // If first NIC wasn't a loopback, the first should have matched due to smaller subnet/better match.
        Assertions.assertEquals( "192.168.10.10", result.getHostAddress());
    }

    @Test
    public void localIPbestMatch() throws Exception {
        LocalAddressInSameSubnetFinder serviceDiscoveryRequestHandler = new LocalAddressInSameSubnetFinder();
        Inet4Address result = serviceDiscoveryRequestHandler.findHostAddress(
            List.of(
                createNetworkInterface(
                    false, true,
                    createInterfaceAddress(
                        InetAddress.getByName("192.168.178.10"),
                        InetAddress.getByName("192.168.178.255"),
                        (short) 24
                    )
                ),
                createNetworkInterface(
                    false, true,
                    createInterfaceAddress(
                        InetAddress.getByName("192.168.10.10"),
                        InetAddress.getByName("192.168.255.255"),
                        (short) 16
                    )
                )
            ),
            (Inet4Address) InetAddress.getByName("192.168.178.15")
        );

        Assertions.assertEquals(result.getHostAddress(), "192.168.178.10");
    }

    private NetworkInterface createNetworkInterface(boolean isLoopback, boolean isUp, InterfaceAddress... ifAddresses) throws SocketException {
        NetworkInterface result = Mockito.mock(NetworkInterface.class);
        Mockito.when(result.isLoopback()).thenReturn(isLoopback);
        Mockito.when(result.isUp()).thenReturn(isUp);
        Mockito.when(result.getInterfaceAddresses()).thenReturn(Arrays.asList(ifAddresses));
        return result;
    }


    private InterfaceAddress createInterfaceAddress(InetAddress address, InetAddress broadcast, short networkPrefixLength) {
        InterfaceAddress result = Mockito.mock(InterfaceAddress.class);
        Mockito.when(result.getAddress()).thenReturn(address);
        Mockito.when(result.getBroadcast()).thenReturn(broadcast);
        Mockito.when(result.getNetworkPrefixLength()).thenReturn(networkPrefixLength);
        return result;
    }
}
