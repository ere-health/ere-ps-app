package health.ere.ps.service.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

public class ClasspathResolverURIAdapter implements ResourceResolver {

    private static final Logger log = Logger.getLogger(ClasspathResolverURIAdapter.class.getName());

    private final ResourceResolver wrapped;

    public ClasspathResolverURIAdapter() {
        this.wrapped = ResourceResolverFactory.createDefaultResourceResolver();
    }

    @Override
    public Resource getResource(URI uri) throws IOException {
        log.info("Search resource for Apache FOP: " + uri.toString());
        if (uri.getScheme().equals("classpath")) {
            URL url = getClass().getClassLoader().getResource(uri.getSchemeSpecificPart());
            return new Resource(url.openStream());
        } else {
            return wrapped.getResource(uri);
        }
    }

    @Override
    public OutputStream getOutputStream(URI uri) throws IOException {
        return wrapped.getOutputStream(uri);
    }
}