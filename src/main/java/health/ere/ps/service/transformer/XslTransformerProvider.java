package health.ere.ps.service.transformer;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.logging.Logger;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;

@ApplicationScoped
@Startup
public class XslTransformerProvider implements XmlTransformerProvider {

    private static final Logger log = Logger.getLogger(XslTransformerProvider.class.getName());

    private TransformerFactory factory;

    private static final Object LOCK = new Object();

    @PostConstruct
    public void init() {
        factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_STYLESHEET, "");
        factory.setErrorListener(new ErrorListener() {
            
            private static final String MSG = "Error in XSLT:";

            @Override
            public void warning(TransformerException exception) {
                log.warning(MSG + exception);
            }

            @Override
            public void fatalError(TransformerException exception) {
                log.severe(MSG + exception);
            }

            @Override
            public void error(TransformerException exception) {
                log.severe(MSG + exception);
            }
        });
    }

    @Override
    public Transformer getTransformer(String path) throws TransformerException {
        synchronized (LOCK) {
            InputStream inputStream = getClass().getResourceAsStream(path);
            String systemId = this.getClass().getResource(path).toExternalForm();
            StreamSource xslt = new StreamSource(inputStream, systemId);
            xslt.setPublicId(systemId);
            return factory.newTransformer(xslt);
        }
    }
}
