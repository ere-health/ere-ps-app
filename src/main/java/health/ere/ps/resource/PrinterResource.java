package health.ere.ps.resource;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeImpl;
import com.hp.jipp.encoding.IntRangeType;
import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppOutputStream;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.JobState;
import com.hp.jipp.model.JobStateReason;
import com.hp.jipp.model.MediaCol;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.PrinterState;
import com.hp.jipp.model.Status;
import com.hp.jipp.model.Types;
import com.hp.jipp.trans.IppPacketData;
import com.hp.jipp.trans.IppServerTransport;

import org.apache.pdfbox.pdmodel.PDDocument;

import health.ere.ps.service.ipp.PrinterService;
import kotlin.ranges.IntRange;

@Path("ipp")
public class PrinterResource implements IppServerTransport {

    @Inject
    Event<PDDocument> pdDocumentEvent;

    private static final AtomicInteger printJobId = new AtomicInteger(0);

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
        log.info("Request: "+data);
        IppPacket ippPacket = data.getPacket();
        if(ippPacket.getOperation().equals(Operation.getPrinterAttributes)) {
            List<Attribute<?>> list = new ArrayList<>();
            
            
            if(ippPacket.get(Tag.operationAttributes) != null && ippPacket.get(Tag.operationAttributes).get(Types.requestedAttributes)!= null) {
                for(String requestedAttribute : ippPacket.get(Tag.operationAttributes).get(Types.requestedAttributes)) {
                    System.out.println(requestedAttribute);
                }
            }
            /*

            CUPS requested attributes

            compression-supported
            copies-supported
            cups-version
            document-format-supported
            job-password-encryption-supported
            marker-colors
            marker-high-levels
            marker-levels
            marker-low-levels
            marker-message
            marker-names
            marker-types
            media-col-supported
            multiple-document-handling-supported
            operations-supported
            print-color-mode-supported
            printer-alert
            printer-alert-description
            printer-is-accepting-jobs
            printer-mandatory-job-attributes
            printer-state
            printer-state-message
            printer-state-reasons
            */
            URI printUri = null;
            try {
                printUri = new URI(uri.toString().replace("http", "ipp"));
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            list.add(Types.charsetConfigured.of("utf-8"));
            list.add(Types.charsetSupported.of("utf-8"));
            list.add(Types.compressionSupported.of("none"));
            list.add(Types.documentFormatDefault.of("application/octet-stream"));
            list.add(Types.documentFormatSupported.of("application/octet-stream","application/pdf","application/postscript"));
            list.add(Types.generatedNaturalLanguageSupported.of("de-de"));
            list.add(Types.ippVersionsSupported.of("1.1"));
            list.add(Types.naturalLanguageConfigured.of("de-de"));
            list.add(Types.operationsSupported.of(Operation.printJob, Operation.getJobs, Operation.getPrinterAttributes));
            list.add(Types.pdlOverrideSupported.of("none"));
            list.add(Types.printerIsAcceptingJobs.of(true));
            list.add(Types.printerUri.of(printUri));
            list.add(Types.printerName.of("E-Rezept Drucker"));
            list.add(Types.printerState.of(PrinterState.idle));
            list.add(Types.printerStateReasons.of("none"));
            list.add(Types.printerStateMessage.of(""));
            list.add(Types.printerUpTime.of(1620237165));
            list.add(Types.printerUriSupported.of(printUri));
            list.add(Types.queuedJobCount.of(0));
            list.add(Types.uriAuthenticationSupported.of("none"));
            list.add(Types.uriSecuritySupported.of("none"));
            list.add(Types.copiesSupported.of(new IntRange(1, 9999)));
            //list.add(Types.cupsVersion.of());
            // list.add(Types.jobPasswordEncryptionSupported.of(false));
            //list.add(Types.markerColors.of());
            //list.add(Types.markerHighLevels.of());
            //list.add(Types.markerLevels.of());
            //list.add(Types.markerLowLevels.of());
            //list.add(Types.markerMessage.of());
            //list.add(Types.markerNames.of());
            //list.add(Types.markerTypes.of());
            list.add(Types.mediaSupported.of("iso_a4_210x297mm"));
            list.add(Types.mediaDefault.of("iso_a4_210x297mm"));
            list.add(Types.mediaColSupported.of("media-bottom-margin","media-left-margin","media-right-margin","media-size","media-source","media-top-margin","media-type"));
            list.add(Types.multipleDocumentHandlingSupported.of("separate-documents-uncollated-copies", "separate-documents-collated-copies"));
            list.add(Types.printColorModeSupported.of("monochrome", "color"));
            list.add(Types.printerAlert.of(""));
            list.add(Types.printerAlertDescription.of(""));
            list.add(Types.jobCreationAttributesSupported.of("copies","finishings","ipp-attribute-fidelity","job-hold-until","job-name","job-priority","job-sheets","media","media-col","multiple-document-handling","number-up","output-bin"));
            //list.add(Types.printerMandatoryJobAttributes.of());

            // Examples for attributes: https://github.com/HPInc/jipp/blob/master/jipp-core/src/test/java/com/hp/jipp/encoding/AttributeGroupTest.java
            IppPacket responsePacket = new IppPacket(Status.successfulOk, ippPacket.getRequestId(),
            groupOf(Tag.operationAttributes, Types.attributesCharset.of("utf-8"), Types.attributesNaturalLanguage.of("en-US")),
            groupOf(Tag.printerAttributes, list));
            /*

            Cups delivered attributes:

                    marker-change-time (integer): 0
                        name: marker-change-time
                        integer value: 0
                    printer-config-change-date-time (dateTime): 2021-05-05T17:51:26.0+0000
                        name: printer-config-change-date-time
                        dateTime value: 2021-05-05T17:51:26.0+0000
                    printer-config-change-time (integer): 1620237086
                        name: printer-config-change-time
                        integer value: 1620237086
                    printer-current-time (dateTime): 2021-05-05T17:52:45.0+0000
                        name: printer-current-time
                        dateTime value: 2021-05-05T17:52:45.0+0000
                    printer-dns-sd-name (nameWithoutLanguage): 'Generic PDF @ manuel-XPS-13-9360'
                        name: printer-dns-sd-name
                        nameWithoutLanguage value: 'Generic PDF @ manuel-XPS-13-9360'
                    printer-error-policy (nameWithoutLanguage): 'retry-job'
                        name: printer-error-policy
                        nameWithoutLanguage value: 'retry-job'
                    printer-error-policy-supported (1setOf nameWithoutLanguage): 'abort-job','retry-current-job','retry-job','stop-printer'
                        name: printer-error-policy-supported
                        nameWithoutLanguage value: 'abort-job'
                        nameWithoutLanguage value: 'retry-current-job'
                        nameWithoutLanguage value: 'retry-job'
                        nameWithoutLanguage value: 'stop-printer'
                    printer-icons (uri): 'http://192.168.178.37:631/icons/ere.health.png'
                        name: printer-icons
                        uri value: 'http://192.168.178.37:631/icons/ere.health.png'
                    printer-is-accepting-jobs (boolean): true
                        name: printer-is-accepting-jobs
                        boolean value: true (0x01)
                    printer-is-shared (boolean): true
                        name: printer-is-shared
                        boolean value: true (0x01)
                    printer-is-temporary (boolean): false
                        name: printer-is-temporary
                        boolean value: false (0x00)
                    printer-more-info (uri): 'http://192.168.178.37:631/printers/ere.health'
                        name: printer-more-info
                        uri value: 'http://192.168.178.37:631/printers/ere.health'
                    printer-op-policy (nameWithoutLanguage): 'default'
                        name: printer-op-policy
                        nameWithoutLanguage value: 'default'
                    printer-state (enum): idle
                        name: printer-state
                        printer-state: idle (3)
                    printer-state-change-date-time (dateTime): 2021-05-04T12:01:55.0+0000
                        name: printer-state-change-date-time
                        dateTime value: 2021-05-04T12:01:55.0+0000
                    printer-state-change-time (integer): 1620129715
                        name: printer-state-change-time
                        integer value: 1620129715
                    printer-state-message (textWithoutLanguage): ''
                        name: printer-state-message
                        textWithoutLanguage value: ''
                    printer-state-reasons (keyword): 'none'
                        name: printer-state-reasons
                        keyword value: 'none'
                    printer-type (enum): 135196
                        name: printer-type
                        enum value: 135196
                    printer-up-time (integer): 1620237165
                        name: printer-up-time
                        integer value: 1620237165
                    printer-uri-supported (uri): 'ipp://192.168.178.37:631/printers/ere.health'
                        name: printer-uri-supported
                        uri value: 'ipp://192.168.178.37:631/printers/ere.health'
                    queued-job-count (integer): 0
                        name: queued-job-count
                        integer value: 0
                    uri-authentication-supported (keyword): 'requesting-user-name'
                        name: uri-authentication-supported
                        keyword value: 'requesting-user-name'
                    uri-security-supported (keyword): 'none'
                        name: uri-security-supported
                        keyword value: 'none'
                    printer-name (nameWithoutLanguage): 'ere.health'
                        name: printer-name
                        nameWithoutLanguage value: 'ere.health'
                    printer-location (textWithoutLanguage): ''
                        name: printer-location
                        textWithoutLanguage value: ''
                    printer-geo-location (unknown)
                        out-of-band value: unknown (0x12)
                    printer-info (textWithoutLanguage): 'Generic PDF'
                        name: printer-info
                        textWithoutLanguage value: 'Generic PDF'
                    printer-organization (textWithoutLanguage): ''
                        name: printer-organization
                        textWithoutLanguage value: ''
                    printer-organizational-unit (textWithoutLanguage): ''
                        name: printer-organizational-unit
                        textWithoutLanguage value: ''
                    printer-uuid (uri): 'urn:uuid:b55fddb8-4073-30e7-43ae-400146bc9666'
                        name: printer-uuid
                        uri value: 'urn:uuid:b55fddb8-4073-30e7-43ae-400146bc9666'
                    job-quota-period (integer): 0
                        name: job-quota-period
                        integer value: 0
                    job-k-limit (integer): 0
                        name: job-k-limit
                        integer value: 0
                    job-page-limit (integer): 0
                        name: job-page-limit
                        integer value: 0
                    job-sheets-default (1setOf nameWithoutLanguage): 'none','none'
                        name: job-sheets-default
                        nameWithoutLanguage value: 'none'
                        nameWithoutLanguage value: 'none'
                    device-uri (uri): 'ipp://localhost:8080/ipp/print'
                        name: device-uri
                        uri value: 'ipp://localhost:8080/ipp/print'
                     [truncated]
                     document-format-supported (1setOf mimeMediaType): 'application/octet-stream','application/pdf','application/postscript','application/vnd.adobe-reader-postscript','application/vnd.cups-pdf','application/vnd.cups-pdf-banner','ap
                        name: document-format-supported
                        mimeMediaType value: 'application/octet-stream'
                        mimeMediaType value: 'application/pdf'
                        mimeMediaType value: 'application/postscript'
                        mimeMediaType value: 'application/vnd.adobe-reader-postscript'
                        mimeMediaType value: 'application/vnd.cups-pdf'
                        mimeMediaType value: 'application/vnd.cups-pdf-banner'
                        mimeMediaType value: 'application/vnd.cups-postscript'
                        mimeMediaType value: 'application/vnd.cups-raster'
                        mimeMediaType value: 'application/vnd.cups-raw'
                        mimeMediaType value: 'application/x-cshell'
                        mimeMediaType value: 'application/x-csource'
                        mimeMediaType value: 'application/x-perl'
                        mimeMediaType value: 'application/x-shell'
                        mimeMediaType value: 'image/gif'
                        mimeMediaType value: 'image/jpeg'
                        mimeMediaType value: 'image/png'
                        mimeMediaType value: 'image/pwg-raster'
                        mimeMediaType value: 'image/tiff'
                        mimeMediaType value: 'image/urf'
                        mimeMediaType value: 'image/x-bitmap'
                        mimeMediaType value: 'image/x-photocd'
                        mimeMediaType value: 'image/x-portable-anymap'
                        mimeMediaType value: 'image/x-portable-bitmap'
                        mimeMediaType value: 'image/x-portable-graymap'
                        mimeMediaType value: 'image/x-portable-pixmap'
                        mimeMediaType value: 'image/x-sgi-rgb'
                        mimeMediaType value: 'image/x-sun-raster'
                        mimeMediaType value: 'image/x-xbitmap'
                        mimeMediaType value: 'image/x-xpixmap'
                        mimeMediaType value: 'image/x-xwindowdump'
                        mimeMediaType value: 'text/css'
                        mimeMediaType value: 'text/html'
                        mimeMediaType value: 'text/plain'
                    pwg-raster-document-resolution-supported (1setOf resolution): 300x300dpi,600x600dpi,1200x1200dpi
                        name: pwg-raster-document-resolution-supported
                        resolution value: 300x300dpi
                        resolution value: 600x600dpi
                        resolution value: 1200x1200dpi
                    pwg-raster-document-sheet-back (keyword): 'normal'
                        name: pwg-raster-document-sheet-back
                        keyword value: 'normal'
                    pwg-raster-document-type-supported (1setOf keyword): 'adobergb-8','adobergb-16','black-1','black-8','black-16','cmyk-8','cmyk-16','rgb-8','rgb-16','sgray-1','sgray-8','sgray-16','srgb-8','srgb-16'
                        name: pwg-raster-document-type-supported
                        keyword value: 'adobergb-8'
                        keyword value: 'adobergb-16'
                        keyword value: 'black-1'
                        keyword value: 'black-8'
                        keyword value: 'black-16'
                        keyword value: 'cmyk-8'
                        keyword value: 'cmyk-16'
                        keyword value: 'rgb-8'
                        keyword value: 'rgb-16'
                        keyword value: 'sgray-1'
                        keyword value: 'sgray-8'
                        keyword value: 'sgray-16'
                        keyword value: 'srgb-8'
                        keyword value: 'srgb-16'
                    copies-default (integer): 1
                        name: copies-default
                        integer value: 1
                    document-format-default (mimeMediaType): 'application/octet-stream'
                        name: document-format-default
                        mimeMediaType value: 'application/octet-stream'
                    job-cancel-after-default (integer): 10800
                        name: job-cancel-after-default
                        integer value: 10800
                    job-hold-until-default (keyword): 'no-hold'
                        name: job-hold-until-default
                        keyword value: 'no-hold'
                    job-priority-default (integer): 50
                        name: job-priority-default
                        integer value: 50
                    number-up-default (integer): 1
                        name: number-up-default
                        integer value: 1
                    notify-lease-duration-default (integer): 86400
                        name: notify-lease-duration-default
                        integer value: 86400
                    notify-events-default (keyword): 'job-completed'
                        name: notify-events-default
                        keyword value: 'job-completed'
                    orientation-requested-default (no-value)
                        out-of-band value: no-value (0x13)
                    print-quality-default (enum): normal
                        name: print-quality-default
                        print-quality: normal (4)
                    color-supported (boolean): true
                        name: color-supported
                        boolean value: true (0x01)
                    pages-per-minute (integer): 30
                        name: pages-per-minute
                        integer value: 30
                    pages-per-minute-color (integer): 30
                        name: pages-per-minute-color
                        integer value: 30
                    printer-device-id (textWithoutLanguage): 'MFG:Generic;CMD:PJL,PDF;MDL:PDF Printer;CLS:PRINTER;DES:Generic PDF Printer;DRV:DPDF,R1,M0;'
                        name: printer-device-id
                        textWithoutLanguage value: 'MFG:Generic;CMD:PJL,PDF;MDL:PDF Printer;CLS:PRINTER;DES:Generic PDF Printer;DRV:DPDF,R1,M0;'
                    print-quality-supported (enum): normal
                        name: print-quality-supported
                        print-quality: normal (4)
                    printer-make-and-model (textWithoutLanguage): 'Generic PDF Printer'
                        name: printer-make-and-model
                        textWithoutLanguage value: 'Generic PDF Printer'
                    media-default (keyword): 'iso_a4_210x297mm'
                        name: media-default
                        keyword value: 'iso_a4_210x297mm'
                     [truncated]
                     media-supported (1setOf keyword): 'na_letter_8.5x11in','iso_a4_210x297mm','iso_a5_148x210mm','iso_a6_105x148mm','iso_b5_176x250mm','iso_c5_162x229mm','na_number-10_4.125x9.5in','iso_dl_110x220mm','custom_5x13in_5x13in','iso_c6
                        name: media-supported
                        keyword value: 'na_letter_8.5x11in'
                        keyword value: 'iso_a4_210x297mm'
                        keyword value: 'iso_a5_148x210mm'
                        keyword value: 'iso_a6_105x148mm'
                        keyword value: 'iso_b5_176x250mm'
                        keyword value: 'iso_c5_162x229mm'
                        keyword value: 'na_number-10_4.125x9.5in'
                        keyword value: 'iso_dl_110x220mm'
                        keyword value: 'custom_5x13in_5x13in'
                        keyword value: 'iso_c6_114x162mm'
                        keyword value: 'na_executive_7.25x10.5in'
                        keyword value: 'jis_b5_182x257mm'
                        keyword value: 'jis_b6_128x182mm'
                        keyword value: 'na_legal_8.5x14in'
                        keyword value: 'na_monarch_3.875x7.5in'
                        keyword value: 'custom_68.79x95.25mm_68.79x95.25mm'
                        keyword value: 'na_invoice_5.5x8.5in'
                    media-bottom-margin-supported (integer): 1270
                        name: media-bottom-margin-supported
                        integer value: 1270
                    media-left-margin-supported (integer): 635
                        name: media-left-margin-supported
                        integer value: 635
                    media-right-margin-supported (integer): 635
                        name: media-right-margin-supported
                        integer value: 635
                    media-top-margin-supported (integer): 1270
                        name: media-top-margin-supported
                        integer value: 1270
                    output-bin-supported (keyword): 'face-down'
                        name: output-bin-supported
                        keyword value: 'face-down'
                    output-bin-default (keyword): 'face-down'
                        name: output-bin-default
                        keyword value: 'face-down'
                    print-color-mode-supported (1setOf keyword): 'monochrome','color'
                        name: print-color-mode-supported
                        keyword value: 'monochrome'
                        keyword value: 'color'
                    print-color-mode-default (keyword): 'color'
                        name: print-color-mode-default
                        keyword value: 'color'
                    pwg-raster-document-type-supported (1setOf keyword): 'black_1','sgray_8','srgb_8'
                        name: pwg-raster-document-type-supported
                        keyword value: 'black_1'
                        keyword value: 'sgray_8'
                        keyword value: 'srgb_8'
                    printer-resolution-supported (1setOf resolution): 300x300dpi,600x600dpi,1200x1200dpi
                        name: printer-resolution-supported
                        resolution value: 300x300dpi
                        resolution value: 600x600dpi
                        resolution value: 1200x1200dpi
                    pwg-raster-document-resolution-supported (resolution): 300x300dpi
                        name: pwg-raster-document-resolution-supported
                        resolution value: 300x300dpi
                    printer-resolution-default (resolution): 600x600dpi
                        name: printer-resolution-default
                        resolution value: 600x600dpi
                    pwg-raster-document-sheet-back (keyword): 'normal'
                        name: pwg-raster-document-sheet-back
                        keyword value: 'normal'
                    sides-supported (1setOf keyword): 'one-sided','two-sided-long-edge','two-sided-short-edge'
                        name: sides-supported
                        keyword value: 'one-sided'
                        keyword value: 'two-sided-long-edge'
                        keyword value: 'two-sided-short-edge'
                    sides-default (keyword): 'one-sided'
                        name: sides-default
                        keyword value: 'one-sided'
                    printer-commands (keyword): 'none'
                        name: printer-commands
                        keyword value: 'none'
                    port-monitor (nameWithoutLanguage): 'none'
                        name: port-monitor
                        nameWithoutLanguage value: 'none'
                    port-monitor-supported (nameWithoutLanguage): 'none'
                        name: port-monitor-supported
                        nameWithoutLanguage value: 'none'
                    finishings-supported (enum): none
                        name: finishings-supported
                        finishings: none (3)
                    finishings-default (enum): none
                        name: finishings-default
                        finishings: none (3)
                    charset-configured (charset): 'utf-8'
                        name: charset-configured
                        charset value: 'utf-8'
                    charset-supported (1setOf charset): 'us-ascii','utf-8'
                        name: charset-supported
                        charset value: 'us-ascii'
                        charset value: 'utf-8'
                    compression-supported (1setOf keyword): 'none','gzip'
                        name: compression-supported
                        keyword value: 'none'
                        keyword value: 'gzip'
                    copies-supported (rangeOfInteger): 1-9999
                        name: copies-supported
                        rangeOfInteger value: 1-9999
                    cups-version (textWithoutLanguage): '2.2.7'
                        name: cups-version
                        textWithoutLanguage value: '2.2.7'
                    generated-natural-language-supported (naturalLanguage): 'de-de'
                        name: generated-natural-language-supported
                        naturalLanguage value: 'de-de'
                    ipp-features-supported (keyword): 'subscription-object'
                        name: ipp-features-supported
                        keyword value: 'subscription-object'
                    ipp-versions-supported (1setOf keyword): '1.0','1.1','2.0','2.1'
                        name: ipp-versions-supported
                        keyword value: '1.0'
                        keyword value: '1.1'
                        keyword value: '2.0'
                        keyword value: '2.1'
                    ippget-event-life (integer): 15
                        name: ippget-event-life
                        integer value: 15
                    job-cancel-after-supported (rangeOfInteger): 0-2147483647
                        name: job-cancel-after-supported
                        rangeOfInteger value: 0-2147483647
                     [truncated]
                     job-creation-attributes-supported (1setOf keyword): 'copies','finishings','ipp-attribute-fidelity','job-hold-until','job-name','job-priority','job-sheets','media','media-col','multiple-document-handling','number-up','output-bi
                        name: job-creation-attributes-supported
                        keyword value: 'copies'
                        keyword value: 'finishings'
                        keyword value: 'ipp-attribute-fidelity'
                        keyword value: 'job-hold-until'
                        keyword value: 'job-name'
                        keyword value: 'job-priority'
                        keyword value: 'job-sheets'
                        keyword value: 'media'
                        keyword value: 'media-col'
                        keyword value: 'multiple-document-handling'
                        keyword value: 'number-up'
                        keyword value: 'output-bin'
                        keyword value: 'orientation-requested'
                        keyword value: 'page-ranges'
                        keyword value: 'print-color-mode'
                        keyword value: 'print-quality'
                        keyword value: 'printer-resolution'
                        keyword value: 'sides'
                    job-hold-until-supported (1setOf keyword): 'no-hold','indefinite','day-time','evening','night','second-shift','third-shift','weekend'
                        name: job-hold-until-supported
                        keyword value: 'no-hold'
                        keyword value: 'indefinite'
                        keyword value: 'day-time'
                        keyword value: 'evening'
                        keyword value: 'night'
                        keyword value: 'second-shift'
                        keyword value: 'third-shift'
                        keyword value: 'weekend'
                    job-ids-supported (boolean): true
                        name: job-ids-supported
                        boolean value: true (0x01)
                    job-k-octets-supported (rangeOfInteger): 0-455730536
                        name: job-k-octets-supported
                        rangeOfInteger value: 0-455730536
                    job-priority-supported (integer): 100
                        name: job-priority-supported
                        integer value: 100
                     [truncated]
                     job-settable-attributes-supported (1setOf keyword): 'copies','finishings','job-hold-until','job-name','job-priority','media','media-col','multiple-document-handling','number-up','output-bin','orientation-requested','page-range
                        name: job-settable-attributes-supported
                        keyword value: 'copies'
                        keyword value: 'finishings'
                        keyword value: 'job-hold-until'
                        keyword value: 'job-name'
                        keyword value: 'job-priority'
                        keyword value: 'media'
                        keyword value: 'media-col'
                        keyword value: 'multiple-document-handling'
                        keyword value: 'number-up'
                        keyword value: 'output-bin'
                        keyword value: 'orientation-requested'
                        keyword value: 'page-ranges'
                        keyword value: 'print-color-mode'
                        keyword value: 'print-quality'
                        keyword value: 'printer-resolution'
                        keyword value: 'sides'
                    job-sheets-supported (1setOf nameWithoutLanguage): 'none','classified','confidential','form','secret','standard','topsecret','unclassified'
                        name: job-sheets-supported
                        nameWithoutLanguage value: 'none'
                        nameWithoutLanguage value: 'classified'
                        nameWithoutLanguage value: 'confidential'
                        nameWithoutLanguage value: 'form'
                        nameWithoutLanguage value: 'secret'
                        nameWithoutLanguage value: 'standard'
                        nameWithoutLanguage value: 'topsecret'
                        nameWithoutLanguage value: 'unclassified'
                    jpeg-k-octets-supported (rangeOfInteger): 0-455730536
                        name: jpeg-k-octets-supported
                        rangeOfInteger value: 0-455730536
                    jpeg-x-dimension-supported (rangeOfInteger): 0-65535
                        name: jpeg-x-dimension-supported
                        rangeOfInteger value: 0-65535
                    jpeg-y-dimension-supported (rangeOfInteger): 1-65535
                        name: jpeg-y-dimension-supported
                        rangeOfInteger value: 1-65535
                    media-col-supported (1setOf keyword): 'media-bottom-margin','media-left-margin','media-right-margin','media-size','media-source','media-top-margin','media-type'
                        name: media-col-supported
                        keyword value: 'media-bottom-margin'
                        keyword value: 'media-left-margin'
                        keyword value: 'media-right-margin'
                        keyword value: 'media-size'
                        keyword value: 'media-source'
                        keyword value: 'media-top-margin'
                        keyword value: 'media-type'
                    multiple-document-handling-supported (1setOf keyword): 'separate-documents-uncollated-copies','separate-documents-collated-copies'
                        name: multiple-document-handling-supported
                        keyword value: 'separate-documents-uncollated-copies'
                        keyword value: 'separate-documents-collated-copies'
                    multiple-document-jobs-supported (boolean): true
                        name: multiple-document-jobs-supported
                        boolean value: true (0x01)
                    multiple-operation-time-out (integer): 300
                        name: multiple-operation-time-out
                        integer value: 300
                    multiple-operation-time-out-action (keyword): 'process-job'
                        name: multiple-operation-time-out-action
                        keyword value: 'process-job'
                    natural-language-configured (naturalLanguage): 'de-de'
                        name: natural-language-configured
                        naturalLanguage value: 'de-de'
                    notify-attributes-supported (1setOf keyword): 'printer-state-change-time','notify-lease-expiration-time','notify-subscriber-user-name'
                        name: notify-attributes-supported
                        keyword value: 'printer-state-change-time'
                        keyword value: 'notify-lease-expiration-time'
                        keyword value: 'notify-subscriber-user-name'
                    notify-lease-duration-supported (rangeOfInteger): 0-2147483647
                        name: notify-lease-duration-supported
                        rangeOfInteger value: 0-2147483647
                    notify-max-events-supported (integer): 100
                        name: notify-max-events-supported
                        integer value: 100
                     [truncated]
                     notify-events-supported (1setOf keyword): 'job-completed','job-config-changed','job-created','job-progress','job-state-changed','job-stopped','printer-added','printer-changed','printer-config-changed','printer-deleted','printe
                        name: notify-events-supported
                        keyword value: 'job-completed'
                        keyword value: 'job-config-changed'
                        keyword value: 'job-created'
                        keyword value: 'job-progress'
                        keyword value: 'job-state-changed'
                        keyword value: 'job-stopped'
                        keyword value: 'printer-added'
                        keyword value: 'printer-changed'
                        keyword value: 'printer-config-changed'
                        keyword value: 'printer-deleted'
                        keyword value: 'printer-finishings-changed'
                        keyword value: 'printer-media-changed'
                        keyword value: 'printer-modified'
                        keyword value: 'printer-restarted'
                        keyword value: 'printer-shutdown'
                        keyword value: 'printer-state-changed'
                        keyword value: 'printer-stopped'
                        keyword value: 'server-audit'
                        keyword value: 'server-restarted'
                        keyword value: 'server-started'
                        keyword value: 'server-stopped'
                    notify-pull-method-supported (keyword): 'ippget'
                        name: notify-pull-method-supported
                        keyword value: 'ippget'
                    notify-schemes-supported (1setOf keyword): 'dbus','mailto','rss'
                        name: notify-schemes-supported
                        keyword value: 'dbus'
                        keyword value: 'mailto'
                        keyword value: 'rss'
                    number-up-supported (1setOf integer): 1,2,4,6,9,16
                        name: number-up-supported
                        integer value: 1
                        integer value: 2
                        integer value: 4
                        integer value: 6
                        integer value: 9
                        integer value: 16
                     [truncated]
                     operations-supported (1setOf enum): Print-Job,Validate-Job,Create-Job,Send-Document,Cancel-Job,Get-Job-Attributes,Get-Jobs,Get-Printer-Attributes,Hold-Job,Release-Job,Pause-Printer,Resume-Printer,Purge-Jobs,Set-Printer-Attribu
                        name: operations-supported
                        operations-supported: Print-Job (2)
                        operations-supported: Validate-Job (4)
                        operations-supported: Create-Job (5)
                        operations-supported: Send-Document (6)
                        operations-supported: Cancel-Job (8)
                        operations-supported: Get-Job-Attributes (9)
                        operations-supported: Get-Jobs (10)
                        operations-supported: Get-Printer-Attributes (11)
                        operations-supported: Hold-Job (12)
                        operations-supported: Release-Job (13)
                        operations-supported: Pause-Printer (16)
                        operations-supported: Resume-Printer (17)
                        operations-supported: Purge-Jobs (18)
                        operations-supported: Set-Printer-Attributes (19)
                        operations-supported: Set-Job-Attributes (20)
                        operations-supported: Get-Printer-Supported-Values (21)
                        operations-supported: Create-Printer-Subscriptions (22)
                        operations-supported: Create-Job-Subscriptions (23)
                        operations-supported: Get-Subscription-Attributes (24)
                        operations-supported: Get-Subscriptions (25)
                        operations-supported: Renew-Subscription (26)
                        operations-supported: Cancel-Subscription (27)
                        operations-supported: Get-Notifications (28)
                        operations-supported: Enable-Printer (34)
                        operations-supported: Disable-Printer (35)
                        operations-supported: Hold-New-Jobs (37)
                        operations-supported: Release-Held-New-Jobs (38)
                        operations-supported: Cancel-Jobs (56)
                        operations-supported: Cancel-My-Jobs (57)
                        operations-supported: Close-Job (59)
                        operations-supported: CUPS-Get-Default (16385)
                        operations-supported: CUPS-Get-Printers (16386)
                        operations-supported: CUPS-Add-Modify-Printer (16387)
                        operations-supported: CUPS-Delete-Printer (16388)
                        operations-supported: CUPS-Get-Classes (16389)
                        operations-supported: CUPS-Add-Modify-Class (16390)
                        operations-supported: CUPS-Delete-Class (16391)
                        operations-supported: CUPS-Accept-Jobs (16392)
                        operations-supported: CUPS-Reject-Jobs (16393)
                        operations-supported: CUPS-Set-Default (16394)
                        operations-supported: CUPS-Get-Devices (16395)
                        operations-supported: CUPS-Get-PPDs (16396)
                        operations-supported: CUPS-Move-Job (16397)
                        operations-supported: CUPS-Authenticate-Job (16398)
                        operations-supported: CUPS-Get-PPD (16399)
                        operations-supported: CUPS-Get-Document (16423)
                        operations-supported: Restart-Job (14)
                    orientation-requested-supported (1setOf enum): portrait,landscape,reverse-landscape,reverse-portrait
                        name: orientation-requested-supported
                        orientation: portrait (3)
                        orientation: landscape (4)
                        orientation: reverse-landscape (5)
                        orientation: reverse-portrait (6)
                    page-ranges-supported (boolean): true
                        name: page-ranges-supported
                        boolean value: true (0x01)
                    pdf-k-octets-supported (rangeOfInteger): 0-455730536
                        name: pdf-k-octets-supported
                        rangeOfInteger value: 0-455730536
                    pdf-versions-supported (1setOf keyword): 'adobe-1.2','adobe-1.3','adobe-1.4','adobe-1.5','adobe-1.6','adobe-1.7','iso-19005-1_2005','iso-32000-1_2008','pwg-5102.3'
                        name: pdf-versions-supported
                        keyword value: 'adobe-1.2'
                        keyword value: 'adobe-1.3'
                        keyword value: 'adobe-1.4'
                        keyword value: 'adobe-1.5'
                        keyword value: 'adobe-1.6'
                        keyword value: 'adobe-1.7'
                        keyword value: 'iso-19005-1_2005'
                        keyword value: 'iso-32000-1_2008'
                        keyword value: 'pwg-5102.3'
                    pdl-override-supported (keyword): 'attempted'
                        name: pdl-override-supported
                        keyword value: 'attempted'
                    printer-get-attributes-supported (keyword): 'document-format'
                        name: printer-get-attributes-supported
                        keyword value: 'document-format'
                    printer-op-policy-supported (1setOf nameWithoutLanguage): 'authenticated','default'
                        name: printer-op-policy-supported
                        nameWithoutLanguage value: 'authenticated'
                        nameWithoutLanguage value: 'default'
                    printer-settable-attributes-supported (1setOf keyword): 'printer-geo-location','printer-info','printer-location','printer-organization','printer-organizational-unit'
                        name: printer-settable-attributes-supported
                        keyword value: 'printer-geo-location'
                        keyword value: 'printer-info'
                        keyword value: 'printer-location'
                        keyword value: 'printer-organization'
                        keyword value: 'printer-organizational-unit'
                    server-is-sharing-printers (boolean): true
                        name: server-is-sharing-printers
                        boolean value: true (0x01)
                    which-jobs-supported (1setOf keyword): 'completed','not-completed','aborted','all','canceled','pending','pending-held','processing','processing-stopped'
                        name: which-jobs-supported
                        keyword value: 'completed'
                        keyword value: 'not-completed'
                        keyword value: 'aborted'
                        keyword value: 'all'
                        keyword value: 'canceled'
                        keyword value: 'pending'
                        keyword value: 'pending-held'
                        keyword value: 'processing'
                        keyword value: 'processing-stopped'
                end-of-attributes-tag*/
            IppPacketData serverResponse = new IppPacketData(responsePacket, null);
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
            pdDocumentEvent.fireAsync(PDDocument.load(data.getData()));
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
