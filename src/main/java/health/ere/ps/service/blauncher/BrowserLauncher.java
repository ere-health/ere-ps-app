package health.ere.ps.service.blauncher;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@ApplicationScoped
public class BrowserLauncher {


    @ConfigProperty(name = "ere.browser-launcher.autoLaunch")
    boolean autoLaunch;

    @ConfigProperty(name = "quarkus.http.port")
    String port;

    private final String ip = "http://localhost";
    private final String index = "/frontend/app/src/index.html";

    private URI getFrontendURI() throws URISyntaxException {
        return new URI(String.format("%s:%s%s", ip, port, index));
    }

    private void openFrontendPageInDefaultBrowser() throws URISyntaxException, IOException {
        if (autoLaunch && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            Desktop.getDesktop().browse(getFrontendURI());
    }

    void onStart(@Observes StartupEvent ev) throws URISyntaxException, IOException {
        openFrontendPageInDefaultBrowser();
    }
}