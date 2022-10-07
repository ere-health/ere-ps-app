package health.ere.ps.service.ipp;

import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.model.Operation;
import com.hp.jipp.trans.IppPacketData;
import health.ere.ps.model.ipp.IppPrinter;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class PrinterServiceTest {

    @Inject
    PrinterService printerService;

    @BeforeAll
    public static void setup() {
        IppPrinter mockIppPrinter = mock(IppPrinter.class);
        when(mockIppPrinter.getOperationAttributes()).thenReturn(Collections.emptyList());
        when(mockIppPrinter.getPrinterAttributes(any())).thenReturn(Collections.emptyList());

        QuarkusMock.installMockForType(mockIppPrinter, IppPrinter.class);
    }

    @Test
    public void testBuildPrinterAttributesOperationPacket() throws IOException {
        URI uri = URI.create("https://example.com");
        IppPacketData data = new IppPacketData(new IppPacket(Operation.getPrinterAttributes, 1));
        IppPacketData packetData = printerService.handleIppPacketData(uri, data);
        assertEquals(1, packetData.getPacket().getRequestId());
    }

    @Test
    public void testBuildDefaultPacket() throws IOException {
        URI uri = URI.create("https://example.com");
        IppPacketData data = new IppPacketData(new IppPacket(Operation.updateDocumentStatus,1));
        IppPacketData packetData = printerService.handleIppPacketData(uri, data);
        assertEquals(1, packetData.getPacket().getRequestId());
    }
}
