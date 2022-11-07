package health.ere.ps.jsonb;

import javax.json.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.annotations.JsonAdapter;
import org.hl7.fhir.r4.model.Bundle;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class BundleAdapterTest {


    @Test
    @DisplayName("adaptToJson  returns JsonObject ")
    void adaptToJsonShouldReturnJsonObject() {
        Bundle bundle = new Bundle();
        BundleAdapter adapter = new BundleAdapter();
        JsonObject actual = adapter.adaptToJson(bundle);
        assertEquals("Bundle", actual.getString("resourceType"));
        assertEquals(1, actual.keySet().size());

    }


    @Test
    @DisplayName("adaptFromJson  returns bundle ")
    void adaptFromJsonShouldReturnBundle() {


    }
}