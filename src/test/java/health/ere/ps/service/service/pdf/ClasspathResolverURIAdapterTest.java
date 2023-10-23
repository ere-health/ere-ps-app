package health.ere.ps.service.pdf;

import org.apache.xml.security.stax.ext.ResourceResolver;
import org.apache.xmlgraphics.io.Resource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ClasspathResolverURIAdapterTest {

    @Test
    public void testGetResourceFromClasspath() throws IOException {
        ClasspathResolverURIAdapter resolver = new ClasspathResolverURIAdapter();
        URI classpathURI = URI.create("classpath:/example-resource.txt");

        Resource resource = resolver.getResource(classpathURI);

        assertNotNull(resource);
        InputStream inputStream = resource.nullInputStream();
        assertNotNull(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        String content = new String(outputStream.toByteArray(), "UTF-8");
        assertEquals("Example resource content", content);
        inputStream.close();
    }

    @Test
    public void testGetResourceFromDefaultResolver() throws IOException {
        ResourceResolver mockedResolver = mock(ResourceResolver.class);
        URI nonClasspathURI = URI.create("http://example.com/resource");

        ((ClasspathResolverURIAdapter) verify(mockedResolver, times(1))).getResource(nonClasspathURI);
    }

    @Test
    public void testGetOutputStream() throws IOException {
        ResourceResolver mockedResolver = mock(ResourceResolver.class);
        URI uri = URI.create("http://example.com/resource");
    }
}
