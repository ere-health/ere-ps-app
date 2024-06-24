package health.ere.ps.service.common.util;

import org.apache.commons.lang3.StringUtils;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

/**
 * A Utility class used to extract information of particular importance from a bundle json string.
 */
public class BundleJsonInfoExtractor {
    private static final Logger logger = Logger.getLogger(BundleJsonInfoExtractor.class.getName());

    /**
     * Returns a map of info contained in a bundle json string with the following map keys:
     * <p>
     * bundleTimestamp, patientFirstName, patientLastName, patientBirthDate, medicationText
     * <p>
     * The map will contain blank string values for map keys associated with the respective bundle
     * values if those values cannot be found in the bundle json string.
     * <p>
     * The values extracted from the bundle can be used to help in uniquely identifying the bundle
     * among a batch of bundles.
     * <p>
     * Note: The hapi parser library is not used for this implementation as it will require
     * conversion to a Java Bundle object which may result in exceptions being generated if the
     * formatting of the structure and string representations of data types in the bundle json
     * string are incorrect.
     *
     * @param bundleJson the bundle json string
     * @return returns: A map of default info extracted from the bundle json string if the bundle
     * json string parameter is not null or a blank string.
     * <p>
     * An empty map if the bundle json string parameter is null or a blank string.
     * <p>
     * An empty or partially filled map if an exception is thrown while data is being extracted from
     * the bundle json string.
     */
    public static Map<String, String> extractDefaultBundleInfoFromBundleJson(String bundleJson) {
        Map<String, String> bundleInfoMap = new HashMap<>(1);

        // Absolutely no exceptions can be thrown from this method as it's used in the EreLogger
        // class.
        try {
            if (StringUtils.isNotBlank(bundleJson)) {
                try (JsonReader jsonReader = Json.createReader(new StringReader(bundleJson))) {
                    JsonObject jsonObject = jsonReader.readObject();

                    bundleInfoMap.put("bundleTimestamp", jsonObject.getString("timestamp", ""));

                    JsonArray entryArray = jsonObject.getJsonArray("entry");

                    if (entryArray != null) {
                        bundleInfoMap.put("patientFirstName",
                                entryArray.getJsonObject(3)
                                        .getJsonObject("resource").getJsonArray("name")
                                        .getJsonObject(0).getJsonArray("given")
                                        .getString(0, ""));

                        bundleInfoMap.put("patientLastName",
                                entryArray.getJsonObject(3)
                                        .getJsonObject("resource").getJsonArray("name")
                                        .getJsonObject(0)
                                        .getString("family", ""));

                        bundleInfoMap.put("patientBirthDate",
                                entryArray.getJsonObject(3)
                                        .getJsonObject("resource")
                                        .getString("birthDate", ""));

                        bundleInfoMap.put("medicationText",
                                entryArray.getJsonObject(2)
                                        .getJsonObject("resource").getJsonObject("code").getString(
                                        "text", ""));
                    }
                }
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Error occurred while extracting bundle ID information", e);
        }

        return bundleInfoMap;
    }


}
