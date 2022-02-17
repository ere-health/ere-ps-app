package health.ere.ps.service.gematik;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class KIMFlowtype169ServiceTest {

    @Test
    public void testSendERezeptToKIMAddress() {
      String fromKimAddress = "incentergy_test_02@arv.kim.telematik-test";
      String toKimAddress = "incentergy_test_02@arv.kim.telematik-test";
      String smtpHostServer = "localhost";
      String smtpUser = "incentergy_test_02@arv.kim.telematik-test#10.30.8.6:465#Incentergy#Incentergy#1786_A1";
      String smtpPassword = "z6g9ewzmSURGfP2";
      String eRezeptToken = "Task/4711/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea";

      KIMFlowtype169Service kIMFlowtype169Service = new KIMFlowtype169Service();
      kIMFlowtype169Service.sendERezeptToKIMAddress(fromKimAddress, toKimAddress, smtpHostServer, smtpUser, smtpPassword, eRezeptToken);
    }
}
