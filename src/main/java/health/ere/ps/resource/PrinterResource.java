package health.ere.ps.resource;


import java.io.*;
import java.net.URI;
import java.util.logging.Logger;

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
import com.hp.jipp.trans.IppPacketData;
import com.hp.jipp.trans.IppServerTransport;
import health.ere.ps.service.ipp.PrinterService;


@Path("ipp")
public class PrinterResource implements IppServerTransport {

    @Inject
    PrinterService printerService;

    private final String IPP_MEDIA_TYPE = "application/ipp";

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
        IppPacketData serverResponse = printerService.handleIppPacketData(uri, data);
        log.info("Response: "+serverResponse);
        return serverResponse;
    }
}
