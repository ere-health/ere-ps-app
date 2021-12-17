package health.ere.ps.service.ssh;

import org.junit.jupiter.api.Test;

class SSHServiceTest {

	@Test
	void testInit() {
		SSHService sshService = new SSHService();
		sshService.init();
	}

}
