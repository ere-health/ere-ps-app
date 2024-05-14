package health.ere.ps.resource.config;

import health.ere.ps.jsonb.BundleAdapter;
import health.ere.ps.jsonb.ByteAdapter;
import health.ere.ps.jsonb.DurationAdapter;
import health.ere.ps.jsonb.ThrowableAdapter;
import jakarta.annotation.Priority;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.BinaryDataStrategy;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(99)
@Produces({
    MediaType.APPLICATION_JSON
})
public class JSONConfigurator implements ContextResolver<Jsonb> {

    static JsonbConfig customConfig = new JsonbConfig()
            .setProperty(JsonbConfig.FORMATTING, true)
            .withAdapters(new BundleAdapter())
            .withAdapters(new ByteAdapter())
            .withAdapters(new ThrowableAdapter())
            .withAdapters(new DurationAdapter());
    public static Jsonb jsonbFactory = JsonbBuilder.create(customConfig);
    
    @Override
    public Jsonb getContext(Class<?> type) {
        return jsonbFactory;
    }
}