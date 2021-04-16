package health.ere.ps.resource;

import health.ere.ps.service.ipp.PrinterService;
import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppOutputStream;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.model.Operation;
import com.hp.jipp.trans.IppPacketData;
import com.hp.jipp.trans.IppServerTransport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("ipp")
public class PrinterResource implements IppServerTransport {
    @Inject
    PrinterService printerService;

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
            return Response.ok(output.toByteArray()).build();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public IppPacketData handle(URI uri, IppPacketData data) throws IOException {
        log.info(uri+" was called ");
        log.info("Data: "+data);
        IppPacket ippPacket = data.getPacket();
        if(ippPacket.getOperation().equals(Operation.getPrinterAttributes)) {
            // TODO: Generate default printer attributes
        }

        if(ippPacket.getOperation().equals(Operation.printJob)) {
            // TODO: check for mime type, for the moment, expect PDF
            printerService.print(data.getData());
        }

        return data;
    }

}
