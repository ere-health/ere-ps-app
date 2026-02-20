package health.ere.ps.service.transformer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

public interface XmlTransformerProvider {

    Transformer getTransformer(String path) throws TransformerException;
}
