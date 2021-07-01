package health.ere.ps.service.fhir.bundle;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import health.ere.ps.exception.bundle.EreParseException;

public class EreBundle extends Bundle {

    protected Bundle bundle;
    protected Map<String, String> templateMap = new HashMap<>();
    protected String jsonTemplateForBundle;

    public EreBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public EreBundle(Map<String, String> templateMap) {
        this.templateMap = templateMap;
    }

    public EreBundle() {

    }

    public String encodeToJson() {

        try (InputStream is = getClass().getResourceAsStream(
                "/bundle-samples/FEbundleTemplate.json")) {

            if(MapUtils.isEmpty(templateMap)) {
                throw new EreParseException("Error. Bundle object must not have a null or empty " +
                        " template map object!");
            }

            jsonTemplateForBundle = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            templateMap.keySet().forEach(key ->
                    jsonTemplateForBundle = jsonTemplateForBundle.replace(
                            key, templateMap.get(key)));
        } catch (EreParseException | IOException e) {
            throw new IllegalStateException("Error: Unable to serialise bundle to json!", e);
        }

        return jsonTemplateForBundle;
    }

    public void setTimestampOnField(String fieldName, Date date) {
        Date tempDate = date;

        if(templateMap != null && StringUtils.isNotBlank(fieldName)) {
            if(date == null) {
                tempDate = new Date();
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

            templateMap.put(fieldName, dateFormat.format(tempDate));
        }
    }

}
