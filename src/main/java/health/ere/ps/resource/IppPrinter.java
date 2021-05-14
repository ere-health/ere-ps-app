package health.ere.ps.resource;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Status;
import com.hp.jipp.trans.IppPacketData;

import java.net.URI;
import java.util.*;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.model.Types.*;


public class IppPrinter {

    private final Date startTime;
    private final List<Attribute<?>> defaultPrinterAttributes;

    public IppPrinter() {
        this.startTime = new Date();
        defaultPrinterAttributes = Arrays.asList(DefaultAttributes.PRINTER_ATTRIBUTES);
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
}