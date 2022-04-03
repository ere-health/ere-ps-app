package health.ere.ps.service.fhir.bundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class EreBundleTest {

    private EreBundle ereBundle;
    private Map<String, String> templateMap = new HashMap<>();
    private String jsonTemplateForBundle;

    @Test
    public void testSuccessfulEncodeToJson() {
        this.templateMap.put("timestamp", "randomString12345"); // "timestamp" is one of the fields of the FEbundleTemplate.json
        this.ereBundle = new EreBundle(this.templateMap);
        this.jsonTemplateForBundle = this.ereBundle.encodeToJson();
        assertTrue(this.jsonTemplateForBundle.contains("randomString12345"));
    }

    @Test
    public void testUnsuccessfulEncodeToJsonDueToEmptyTemplateMap1() { // An empty templateMap is passed to the ereBundle
        this.ereBundle = new EreBundle(this.templateMap);
        assertThrows(IllegalStateException.class, () -> this.ereBundle.encodeToJson());
    }

    @Test
    public void testUnsuccessfulEncodeToJsonDueToEmptyTemplateMap2() { // No templateMap is passed to the ereBundle
        this.ereBundle = new EreBundle();
        assertThrows(IllegalStateException.class, () -> this.ereBundle.encodeToJson());
    }

    @Test
    public void testSuccessfulSetTimestampOnField1() { // When no Date is specified
        this.ereBundle = new EreBundle(this.templateMap);
        Date currentDate = new Date();
        this.ereBundle.setTimestampOnField("fieldName", null);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        assertEquals(dateFormat.format(currentDate) + "Z", this.ereBundle.templateMap.get("fieldName"));
    }

    @Test
    public void testSuccessfulSetTimestampOnField2() { // When a Date is specified
        this.ereBundle = new EreBundle(this.templateMap);
        Date randomDate = new Date(TimeUnit.SECONDS.toMillis(1628215200L)); // Fri Aug 06 03:00:00 WEST 2021
        this.ereBundle.setTimestampOnField("fieldName", randomDate);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        assertEquals(dateFormat.format(randomDate) + "Z", this.ereBundle.templateMap.get("fieldName")); // 2021-08-06T03:00:00Z
    }
    
    @Test
    public void testUnsuccessfulSetTimestampOnFieldDueToBlankFieldName() {
        this.ereBundle = new EreBundle(this.templateMap);
        this.ereBundle.setTimestampOnField("", null);
        assertTrue(this.ereBundle.templateMap.isEmpty());
    }
}
