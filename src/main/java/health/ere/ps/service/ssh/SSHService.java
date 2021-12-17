package health.ere.ps.service.ssh;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

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
        sshServer.setShellFactory(new ProcessShellFactory("/bin/bash","/bin/bash", "-i", "-l"));
		try {
			log.info("Starting SSH server on port: "+PORT);
			sshServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new SSHService().init();
	}
}
