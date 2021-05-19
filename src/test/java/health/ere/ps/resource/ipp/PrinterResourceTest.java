package health.ere.ps.resource.ipp;

import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.trans.IppPacketData;

import static com.hp.jipp.model.Types.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import health.ere.ps.resource.HttpIppClientTransport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PrinterResourceTest {

    @Disabled
    @Test
    void handle() throws IOException {
        HttpIppClientTransport transport = new HttpIppClientTransport();
        URI uri = URI.create("http://localhost:8080/ipp/print");
        IppPacket printRequest = IppPacket.printJob(uri)
                .putOperationAttributes(documentFormat.of("application/pdf"))
                .build();
        InputStream input = getClass().getResourceAsStream("/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf");
        transport.sendData(uri,
            new IppPacketData(printRequest, input));
        System.out.println("Done");
    }

    @Disabled
    @Test
    void getPrinterAttributes() throws IOException {
        HttpIppClientTransport transport = new HttpIppClientTransport();
        URI uri = URI.create("http://localhost:8080/ipp/print");
        IppPacket printRequest = IppPacket.getPrinterAttributes(uri)
                .build();
        IppPacketData ippPacketData = transport.sendData(uri, new IppPacketData(printRequest, null));
        IppPacket packet = ippPacketData.getPacket();

        assertNotNull(packet);
        assertPacketComplianceWithWindows(packet);
        assertPrinterAttributesComplianceWithWindows(packet);
        assertPrinterAttributes(packet);
        System.out.println("Done");
    }

    private void assertPrinterAttributes(IppPacket packet) {
        AttributeGroup attributes = packet.get(Tag.printerAttributes);
        assertNotNull(attributes);
        assertTrue(attributes.contains(charsetSupported.of("utf-8")));
        assertTrue(attributes.contains(charsetConfigured.of("utf-8")));
        assertTrue(attributes.contains(naturalLanguageConfigured.of("de-DE")));
        assertTrue(attributes.contains(documentFormatSupported.of("application/pdf", "application/octet-stream")));
    }

    void assertPacketComplianceWithWindows(IppPacket packet) {
        assertEquals(packet.getVersionNumber(), 0x100); // VersionNumber = 1.0
    }

    void assertPrinterAttributesComplianceWithWindows(IppPacket packet) {
        AttributeGroup attributes = packet.get(Tag.printerAttributes);
        assertNotNull(attributes);
        assertTrue(attributes.contains(ippVersionsSupported.of("1.0")));
        assertTrue(attributes.contains(printerIsAcceptingJobs.of(true)));
    }

}