package health.ere.ps.resource;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.*;
import com.hp.jipp.trans.IppPacketData;
import health.ere.ps.event.PDDocumentEvent;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.enterprise.event.Event;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.model.Types.*;


public class IppPrinter {

    private final Date startTime;
    private final List<Attribute<?>> defaultPrinterAttributes;
    private AtomicInteger printJobId = new AtomicInteger(0);
    Event<PDDocumentEvent> pdDocumentEvent;

    public IppPrinter(Event<PDDocumentEvent> pdDocumentEvent) {
        this.startTime = new Date();
        defaultPrinterAttributes = Arrays.asList(DefaultAttributes.PRINTER_ATTRIBUTES);
        this.pdDocumentEvent = pdDocumentEvent;
    }


    private List<Attribute<?>> getPrinterAttributes(URI uri) {

        List<Attribute<?>> attributes = new ArrayList<>();
        attributes.addAll(this.defaultPrinterAttributes);
        attributes.addAll(getDynamicPrinterAttributes(uri));
        return attributes;
    }

    private List<Attribute<?>> getDynamicPrinterAttributes(URI uri) {

        Attribute<?>[] attributes = {
                printerUriSupported.of(uri),
                printerIsAcceptingJobs.of(isAcceptingJobs()),
                queuedJobCount.of(getQueuedJobCount()),
                printerUpTime.of(getUpTime()),
                printerCurrentTime.of(Calendar.getInstance()),
        };
        return Arrays.asList(attributes);
    }

    public List<Attribute<?>> getOperationAttributes() {
        return Arrays.asList(DefaultAttributes.OPERATION_ATTRIBUTES);
    }

    private boolean isAcceptingJobs() {
        return true;
    }

    private int getQueuedJobCount() {
        return 0;
    }

    private int getUpTime() {
        return (int) ((new Date().getTime() - startTime.getTime()) / 1000);
    }

    public IppPacketData handleGetPrinterAttributesOperation(URI uri, IppPacketData requestPacketData) {

        IppPacket requestPacket = requestPacketData.getPacket();

        List<Attribute<?>> operationAttributes = getOperationAttributes();
        List<Attribute<?>> printerAttributes = getPrinterAttributes(uri);

        IppPacket packet = new IppPacket(
                DefaultAttributes.VERSION_NUMBER,
                Status.successfulOk.getCode(),
                requestPacket.getRequestId(),
                groupOf(Tag.operationAttributes, operationAttributes),
                groupOf(Tag.printerAttributes, printerAttributes)
        );
        return new IppPacketData(packet, requestPacketData.getData());
    }

    public IppPacketData handleIppPacketData(URI uri, IppPacketData data) throws IOException {

        IppPacket ippPacket = data.getPacket();

        if (ippPacket.getOperation().equals(Operation.getJobs)) {
            IppPacket responsePacket = new IppPacket(Status.successfulOk, ippPacket.getRequestId(),
                    groupOf(Tag.operationAttributes),
                    groupOf(Tag.printerAttributes));
            return new IppPacketData(responsePacket, null);
        } else if (ippPacket.getOperation().equals(Operation.getPrinterAttributes))
            return handleGetPrinterAttributesOperation(uri, data);
        else if (ippPacket.getOperation().equals(Operation.printJob)) {
            // TODO: check for mime type, for the moment, expect PDF
            pdDocumentEvent.fireAsync(new PDDocumentEvent(PDDocument.load(data.getData())));
            IppPacket responsePacket = IppPacket.jobResponse(
                    Status.successfulOk, ippPacket.getRequestId(), uri.resolve("/job/" + printJobId.incrementAndGet()),
                    JobState.pending,
                    Collections.singletonList(JobStateReason.accountClosed))
                    .putAttributes(Tag.operationAttributes, Types.printerUri.of(uri))
                    .build();
            return new IppPacketData(responsePacket, null);
        }
        return data;
    }
}