package health.ere.ps.resource;

import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.trans.IppPacketData;
import static com.hp.jipp.model.Types.documentFormat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PrinterResourceTest {

    @Test @Disabled
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

}