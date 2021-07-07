package health.ere.ps.service.common.security;

import javax.xml.ws.BindingProvider;

public interface SoapClient {
    BindingProvider getBindingProvider();
}
