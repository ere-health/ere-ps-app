package health.ere.ps.service.ipp;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;
import com.hp.jipp.trans.IppPacketData;
import health.ere.ps.event.PDDocumentEvent;
import health.ere.ps.model.ipp.DefaultAttributes;
import health.ere.ps.model.ipp.IppPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.net.URI;

@ApplicationScoped
public class PrinterService {

    @Inject
    Event<PDDocumentEvent> pdDocumentEvent;

    @Inject
    IppPrinter printer;

    public IppPacket buildGetPrinterAttributesOperationPacket(URI uri, IppPacketData requestPacketData) {

        IppPacket requestPacket = requestPacketData.getPacket();

        return new IppPacket(
                DefaultAttributes.VERSION_NUMBER,
                Status.successfulOk.getCode(),
                requestPacket.getRequestId(),
                AttributeGroup.groupOf(Tag.operationAttributes, printer.getOperationAttributes()),
                AttributeGroup.groupOf(Tag.printerAttributes, printer.getPrinterAttributes(uri))
        );
    }

    public IppPacket buildPrintJobOperationPacket(URI uri, IppPacketData data) throws IOException {

        IppPacket requestPacket = data.getPacket();
        // TODO: check for mime type, for the moment, expect PDF
        pdDocumentEvent.fireAsync(new PDDocumentEvent(PDDocument.load(data.getData())));

        return new IppPacket(
                DefaultAttributes.VERSION_NUMBER,
                Status.successfulOk.getCode(),
                requestPacket.getRequestId(),
                AttributeGroup.groupOf(Tag.jobAttributes, printer.getJobAttributes(uri)),
                AttributeGroup.groupOf(Tag.operationAttributes, printer.getOperationAttributes())
        );
    }

    IppPacket buildDefaultPacket(IppPacketData data) {

        IppPacket ippPacket = data.getPacket();

        return new IppPacket(
                DefaultAttributes.VERSION_NUMBER,
                Status.successfulOk.getCode(),
                ippPacket.getRequestId(),
                AttributeGroup.groupOf(Tag.operationAttributes),
                AttributeGroup.groupOf(Tag.printerAttributes)
        );
    }

    public IppPacketData handleIppPacketData(URI uri, IppPacketData data) throws IOException {

        IppPacket responsePacket;
        if (data.getPacket().getOperation().equals(Operation.getPrinterAttributes))
            responsePacket = buildGetPrinterAttributesOperationPacket(uri, data);
        else if (data.getPacket().getOperation().equals(Operation.printJob))
            responsePacket = buildPrintJobOperationPacket(uri, data);
        else
            responsePacket = buildDefaultPacket(data);
        return new IppPacketData(responsePacket, data.getData());
    }
}