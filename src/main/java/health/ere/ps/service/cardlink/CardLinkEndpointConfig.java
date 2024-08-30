package health.ere.ps.service.cardlink;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.Decoder;
import jakarta.websocket.Encoder;
import jakarta.websocket.Extension;

import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.Map;

public class CardLinkEndpointConfig implements ClientEndpointConfig {

    private final Configurator configurator;
    private final String serialNumber;

    public CardLinkEndpointConfig(Configurator configurator, String serialNumber) {
        this.configurator = configurator;
        this.serialNumber = serialNumber;
    }

    @Override
    public List<String> getPreferredSubprotocols() {
        return List.of(serialNumber);
    }

    @Override
    public List<Extension> getExtensions() {
        return List.of();
    }

    @Override
    public SSLContext getSSLContext() {
        return null;
    }

    @Override
    public Configurator getConfigurator() {
        return configurator;
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return List.of();
    }

    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return List.of();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return Map.of();
    }
}
