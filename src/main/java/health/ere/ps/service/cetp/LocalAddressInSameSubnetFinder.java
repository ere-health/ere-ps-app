package health.ere.ps.service.cetp;


import com.google.common.annotations.VisibleForTesting;

import java.net.*;
import java.util.Collections;
import java.util.List;

/**
 * Most computers nowadays have multiple (possibly virtual) network interfaces attached and hence possibly listen
 * to multiple ip addresses.
 *
 * Given that we know some external IP address, we can check out our own network interfaces if we have one
 * ip address being in the same subnet as the external ip.
 *
 * This is especially useful in a scenario where we receive a broadcast UDP packet: Broadcasts can only be received
 * if one is in the same subnet as the broadcast sender. Hence, if we know the broadcaster ip, we can pretty
 * confidentally predict the network card (and IP address our lur local host) that received the broadcast to e.g.
 * respond to the broadcaster our own IP they can reach us (e.g. Gematik Konnektor).
 */
public class LocalAddressInSameSubnetFinder {

    public static Inet4Address findLocalIPinSameSubnet(Inet4Address peer) throws SocketException {
        return findLocalIPinSameSubnet(Collections.list(NetworkInterface.getNetworkInterfaces()), peer);
    }

    public static Inet4Address findLocalIPinSameSubnet(List<NetworkInterface> nics, Inet4Address peer) {
        return new LocalAddressInSameSubnetFinder().findHostAddress(nics, peer);
    }

    @VisibleForTesting
    LocalAddressInSameSubnetFinder() {
        // NOOP
    }

    @VisibleForTesting
    Inet4Address findHostAddress(List<NetworkInterface> interfaces, Inet4Address peer) {
        if (peer == null) {
            return null;
        }
        Inet4Address bestMatch = null;
        int bestMatchPrefix = -1;
        for (NetworkInterface ni : interfaces) {
            if (isLoopBack(ni) || !isUp(ni)) {
                continue;
            }

            for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                InetAddress address = ia.getAddress();
                // Only support IPv4 address in this entire class currently.
                if ((address instanceof Inet4Address ip4address) && (ia.getNetworkPrefixLength() > bestMatchPrefix) && isInSubnet(ia, peer)) {
                    bestMatch = ip4address;
                    bestMatchPrefix = ia.getNetworkPrefixLength();
                }
            }
        }

        if (bestMatch != null) {
            return bestMatch;
        }

        return null;
    }

    @VisibleForTesting
    boolean isInSubnet(InterfaceAddress ifa, InetAddress ipAddress) {
        InetAddress networkAddress = ifa.getAddress();
        int networkPrefixLength = ifa.getNetworkPrefixLength();

        byte[] networkAddressBytes = networkAddress.getAddress();
        byte[] ipAddressBytes = ipAddress.getAddress();

        if (networkAddressBytes.length != ipAddressBytes.length) {
            // IPv4 and IPv6 length mismatch
            return false;
        }

        int byteCount = networkAddressBytes.length;
        int bitCount = networkPrefixLength;

        for (int i = 0; i < byteCount; i++) {
            int networkByte = networkAddressBytes[i] & 0xFF;
            int ipByte = ipAddressBytes[i] & 0xFF;

            // Calculate the mask for the current byte
            int mask = (bitCount >= 8) ? 0xFF : (0xFF << (8 - bitCount));

            // Apply the mask and compare
            if ((networkByte & mask) != (ipByte & mask)) {
                return false;
            }

            // Subtract the number of bits we just processed
            bitCount -= 8;
            if (bitCount <= 0) {
                break;
            }
        }

        return true;
    }


    private boolean isLoopBack(NetworkInterface nic) {
        try {
            return nic.isLoopback();
        } catch (SocketException e) {
            // Wrap to RTE, should not happen...
            throw new RuntimeException(e);
        }
    }

    private boolean isUp(NetworkInterface nic) {
        try {
            return nic.isUp();
        } catch (SocketException e) {
            // Wrap to RTE, should not happen...
            throw new RuntimeException(e);
        }
    }
}
