package health.ere.ps.service.common.security;

import java.util.Optional;

import javax.xml.ws.BindingProvider;

public interface SoapClient {
    Optional<BindingProvider> getBindingProvider();
}
