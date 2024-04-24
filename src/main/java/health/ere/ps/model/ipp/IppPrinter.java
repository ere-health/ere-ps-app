package health.ere.ps.model.ipp;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.model.*;

import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hp.jipp.model.Types.*;

@ApplicationScoped
public class IppPrinter {

    private final Date startTime;
    private final List<Attribute<?>> defaultPrinterAttributes;
    private final AtomicInteger printJobId = new AtomicInteger(0);

    public IppPrinter() {
        this.startTime = new Date();
        defaultPrinterAttributes = Arrays.asList(DefaultAttributes.PRINTER_ATTRIBUTES);
    }

    public List<Attribute<?>> getPrinterAttributes(URI uri) {

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

    public List<Attribute<?>> getJobAttributes(URI uri) {
        Attribute<?>[] attributes = {
                jobUri.of(uri.resolve("/job/" + printJobId.incrementAndGet())),
                jobState.of(JobState.pending),
                jobStateReasons.of(JobStateReason.accountClosed),
        };
        return Arrays.asList(attributes);
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
}