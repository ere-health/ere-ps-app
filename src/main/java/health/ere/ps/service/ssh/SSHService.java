package health.ere.ps.service.ssh;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.sshd.common.forward.DefaultForwarderFactory;
import org.apache.sshd.common.session.SessionHeartbeatController.HeartbeatType;
// import org.apache.sshd.common.session.SessionHeartbeatController.HeartbeatType;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;

import io.quarkus.runtime.Startup;

@ApplicationScoped
@Startup
public class SSHService {
	
	private static Logger log = Logger.getLogger(SSHService.class.getName());

	private static final int PORT = 1049;

	@PostConstruct
	public void init() {
		SshServer sshServer = SshServer.setUpDefaultServer();
		sshServer.setPort(PORT);
		sshServer.setHost("0.0.0.0");
		sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshServer.setPasswordAuthenticator((username, password, session) -> {
            return true;
        });
        sshServer.setForwardingFilter(new AcceptAllForwardingFilter());
        sshServer.setSessionHeartbeat(HeartbeatType.IGNORE, Duration.ofSeconds(5));
        sshServer.setShellFactory(new ProcessShellFactory("/bin/sh", "/bin/sh", "-i", "-l"));
        sshServer.setForwarderFactory(DefaultForwarderFactory.INSTANCE);
		try {
			log.info("Starting SSH server on port: "+PORT);
			sshServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                    new FileInputStream("src/test/resources/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
		new SSHService().init();
		System.out.println("Press any Key to stop ...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
