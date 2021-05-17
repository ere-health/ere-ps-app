package health.ere.ps.resource;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppOutputStream;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.JobState;
import com.hp.jipp.model.JobStateReason;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;
import com.hp.jipp.model.Types;
import com.hp.jipp.trans.IppPacketData;
import com.hp.jipp.trans.IppServerTransport;

import org.apache.pdfbox.pdmodel.PDDocument;

import health.ere.ps.event.PDDocumentEvent;

@Path("ipp")
public class PrinterResource implements IppServerTransport {

    @Inject
    Event<PDDocumentEvent> pdDocumentEvent;

    private final IppPrinter printer = new IppPrinter();
    private final String IPP_MEDIA_TYPE = "application/ipp";

    private AtomicInteger printJobId = new AtomicInteger(0);

    private static Logger log = Logger.getLogger(PrinterResource.class.getName());

    @POST
    @Path("/{queue}")
    public Response handle(@PathParam("queue") String queue, @Context UriInfo uriInfo, InputStream stream) throws IOException {
        try {
            IppInputStream inputStream = new IppInputStream(stream);
            IppPacketData data = new IppPacketData(inputStream.readPacket(), inputStream);
            IppPacketData response = handle(uriInfo.getRequestUri(), data);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            new IppOutputStream(output).write(response.getPacket());
            return Response.ok(output.toByteArray(), MediaType.valueOf(IPP_MEDIA_TYPE)).build();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public IppPacketData handle(URI uri, IppPacketData data) throws IOException {
        log.info(uri+" was called ");
        log.info("Request: "+data);
        IppPacket ippPacket = data.getPacket();
        if(ippPacket.getOperation().equals(Operation.getPrinterAttributes)) {
            IppPacketData serverResponse = printer.handleGetPrinterAttributesOperation(uri, data);
            log.info("Response: "+serverResponse);
            return serverResponse;
        }

        if(ippPacket.getOperation().equals(Operation.getJobs)) {
            IppPacket responsePacket = new IppPacket(Status.successfulOk, ippPacket.getRequestId(),
            groupOf(Tag.operationAttributes),
            groupOf(Tag.printerAttributes));
            IppPacketData serverResponse = new IppPacketData(responsePacket, null);
            log.info("Response: "+serverResponse);
            return serverResponse;
        }

        if(ippPacket.getOperation().equals(Operation.printJob)) {
            // TODO: check for mime type, for the moment, expect PDF
            pdDocumentEvent.fireAsync(new PDDocumentEvent(PDDocument.load(data.getData())));
            IppPacket responsePacket = IppPacket.jobResponse(
                Status.successfulOk, ippPacket.getRequestId(), uri.resolve("/job/"+printJobId.incrementAndGet()),
                JobState.pending,
                Collections.singletonList(JobStateReason.accountClosed))
                .putAttributes(Tag.operationAttributes, Types.printerUri.of(uri))
            .build();
            IppPacketData serverResponse = new IppPacketData(responsePacket, null);
            log.info("Response: "+serverResponse);
            return serverResponse;
        }

        return data;
    }

}
