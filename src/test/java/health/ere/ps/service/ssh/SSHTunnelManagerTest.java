package health.ere.ps.service.ssh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import health.ere.ps.event.SSHConnectionOfferingEvent;

public class SSHTunnelManagerTest {

    @Test
    public void test() {
        SSHTunnelManager.SSH_CONNECTIONS_XML_FILE = "target/test-ssh-connections.xml";
        SSHTunnelManager sSHTunnelManager = new SSHTunnelManager();

        sSHTunnelManager.init();

        SSHConnectionOfferingEvent sSHConnectionOfferingEvent = sSHTunnelManager.getNextSSHConnectionOffering(null);

        assertTrue(sSHTunnelManager.acceptSSHConnection(sSHConnectionOfferingEvent.getUser(), sSHConnectionOfferingEvent.getSecret()));

        SSHConnectionOfferingEvent sSHConnectionOfferingEvent2 = sSHTunnelManager.getNextSSHConnectionOffering(null);

        assertEquals(sSHConnectionOfferingEvent.getPorts().get(0)+3, sSHConnectionOfferingEvent2.getPorts().get(0));

        sSHTunnelManager.loadSSHConnections(sSHTunnelManager.getSSHConnectionsFile());

        SSHConnection sshConnection = sSHTunnelManager.sshConnections.getSshConnection().get(sSHConnectionOfferingEvent.getUser());

        assertEquals(sshConnection.getUser(), sSHConnectionOfferingEvent.getUser());
        assertEquals(sshConnection.getSecret(), sSHConnectionOfferingEvent.getSecret());

        sSHTunnelManager.getSSHConnectionsFile().delete();

    }

}
