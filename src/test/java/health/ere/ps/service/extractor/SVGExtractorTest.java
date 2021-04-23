package health.ere.ps.service.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class SVGExtractorTest {

    @Test
    void testExtract() throws URISyntaxException {
        SVGExtractor svgExtractor = new SVGExtractor(getClass().getResource("/svg-extract-templates/Muster-16-Template.svg").toURI());
        Map<String, String> map = svgExtractor.extract(getClass().getResourceAsStream("/muster-16-print-samples/cgm-z1-manuel-blechschmidt.pdf"));
        // assertEquals("insurance=\nautIdem3=\ndate=\nnoctu=\nother=\nbirthdate=\npharmacyDate=\ngrossTotal=\naccident=\ntax1=\npractitionerText=\ninsuranceNumber=\ntax2=\nbvg=\npayor=\nprescription1=\npharmayNumber=\nprescription2=\nprescription3=\nautIdem1=\ntax3=\nwithPayment=\nautIdem2=\naccidentDate=Dr. Zahn\nadditionalPayment=\nwithoutPayment=\nworkAccident=\nfactor2=\nfactor1=\nmedication=\nnameAndAddress=\nsprBedarf=\nfactor3=\naccidentOrganization=lin 1000mg N2\nh alle 8 Std\nbegrPflicht=\npractitionerNumber=\nlocationNumber=\naid=\nstatus=\nvaccination=", map.entrySet().stream().map((e) -> e.getKey()+"="+e.getValue()).collect(Collectors.joining("\n")));
    }

}
