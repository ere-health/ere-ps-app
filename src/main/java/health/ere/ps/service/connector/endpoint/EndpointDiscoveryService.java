package health.ere.ps.service.connector.endpoint;

import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.service.common.security.SecretsManagerService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This service automatically discovers the endpoints that are available at the connector.
 */
@ApplicationScoped
public class EndpointDiscoveryService {
    private static final Logger LOG = Logger.getLogger(EndpointDiscoveryService.class.getName());

    /**
     * Certificate to authenticate at the connector.
     */
    @ConfigProperty(name = "connector.cert.auth.store.file")
    Optional<String> connectorTlsCertAuthStoreFile;

    /**
     * Password of the certificate to authenticate at the connector.
     * The default value is a empty sting, so that the password must not be set.
     */
    @ConfigProperty(name = "connector.cert.auth.store.file.password", defaultValue = "!")
    String connectorTlsCertAuthStorePwd;

    /**
     * Certificate to validate with the connector.
     */
    @ConfigProperty(name = "connector.cert.trust.store.file")
    Optional<String> connectorTlsCertTrustStoreFile;

    /**
     * Password of the certificate to authenticate at the connector.
     * The default value is a empty sting, so that the password must not be set.
     */
    @ConfigProperty(name = "connector.cert.trust.store.file.password", defaultValue = "!")
    String connectorTlsCertTrustStorePwd;

    @ConfigProperty(name = "auth-signature.endpoint.address")
    Optional<String> fallbackAuthSignatureServiceEndpointAddress;

    private String authSignatureServiceEndpointAddress;

    @ConfigProperty(name = "signature-service.endpoint.address")
    Optional<String> fallbackSignatureServiceEndpointAddress;

    private String signatureServiceEndpointAddress;

    @ConfigProperty(name = "certificate-service.endpoint.address")
    Optional<String> fallbackCertificateServiceEndpointAddress;

    private String certificateServiceEndpointAddress;

    @ConfigProperty(name = "event-service.endpoint.address")
    Optional<String> fallbackEventServiceEndpointAddress;

    private String eventServiceEndpointAddress;

    @ConfigProperty(name = "connector.base-uri")
    String connectorBaseUri;

    @ConfigProperty(name = "connector.verify-hostname", defaultValue = "true")
    String connectorVerifyHostname;

    @Inject
    SecretsManagerService secretsManagerService;

    @PostConstruct
    void obtainConfiguration() throws IOException, ParserConfigurationException, SecretsManagerException {
        // code copied from IdpClient.java

        SSLContext sslContext;
        try (FileInputStream fileInputStream = new FileInputStream(connectorTlsCertAuthStoreFile.orElseThrow())) {
            sslContext = secretsManagerService.createSSLContext(fileInputStream,
                    connectorTlsCertAuthStorePwd.toCharArray(),
                    SecretsManagerService.SslContextType.TLS,
                    SecretsManagerService.KeyStoreType.PKCS12);
        } catch (IOException e) {
            throw new SecretsManagerException("SSL transport configuration error.", e);
        }

        ClientBuilder clientBuilder = ClientBuilder.newBuilder()
                .sslContext(sslContext);

        if (!isConnectorVerifyHostnames()) {
            // disable hostname verification
            clientBuilder = clientBuilder.hostnameVerifier(new SSLUtilities.FakeHostnameVerifier());
        }

        Invocation invocation = clientBuilder.build()
                .target(connectorBaseUri)
                .path("/connector.sds")
                .request()
                .buildGet();

        try (InputStream inputStream = invocation.invoke(InputStream.class)) {
            Document document = DocumentBuilderFactory.newDefaultInstance()
                    .newDocumentBuilder()
                    .parse(inputStream);

            Node serviceInformationNode = getNodeWithTag(document.getDocumentElement(), "ServiceInformation");

            if (serviceInformationNode == null) {
                throw new IllegalArgumentException("Could not find single 'ServiceInformation'-tag");
            }

            NodeList serviceNodeList = serviceInformationNode.getChildNodes();

            for (int i = 0, n = serviceNodeList.getLength(); i < n; ++i) {
                Node node = serviceNodeList.item(i);

                if (node.getNodeType() != 1) {
                    // ignore formatting related text nodes
                    continue;
                }

                if (!node.hasAttributes() || node.getAttributes().getNamedItem("Name") == null) {
                    break;
                }

                switch (node.getAttributes().getNamedItem("Name").getTextContent()) {
                    case "AuthSignatureService": {
                        authSignatureServiceEndpointAddress = getEndpoint(node);
                        break;
                    }
                    case "EventService": {
                        eventServiceEndpointAddress = getEndpoint(node);
                        break;
                    }
                    case "CertificateService": {
                        certificateServiceEndpointAddress = getEndpoint(node);
                        break;
                    }
                    case "SignatureService": {
                        signatureServiceEndpointAddress = getEndpoint(node);
                    }
                }
            }

        } catch (SAXException | IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Could not parse connector.sds", e);
        }

        if (authSignatureServiceEndpointAddress == null) {
            authSignatureServiceEndpointAddress = fallbackAuthSignatureServiceEndpointAddress.orElseThrow();
        }
        if (signatureServiceEndpointAddress == null) {
            signatureServiceEndpointAddress = fallbackSignatureServiceEndpointAddress.orElseThrow();
        }
        if (eventServiceEndpointAddress == null) {
            eventServiceEndpointAddress = fallbackEventServiceEndpointAddress.orElseThrow();
        }
        if (certificateServiceEndpointAddress == null) {
            certificateServiceEndpointAddress = fallbackCertificateServiceEndpointAddress.orElseThrow();
        }
    }

    public String getAuthSignatureServiceEndpointAddress() {
        return authSignatureServiceEndpointAddress;
    }

    public String getSignatureServiceEndpointAddress() {
        return signatureServiceEndpointAddress;
    }

    public String getCertificateServiceEndpointAddress() {
        return certificateServiceEndpointAddress;
    }

    public String getEventServiceEndpointAddress() {
        return eventServiceEndpointAddress;
    }

    private boolean isConnectorVerifyHostnames() {
        return !("false".equals(connectorVerifyHostname));
    }

    public void configureSSLTransportContext(BindingProvider bindingProvider) throws SecretsManagerException, FileNotFoundException {

        secretsManagerService.configureSSLTransportContext(
                connectorTlsCertAuthStoreFile.orElse(null),
                connectorTlsCertAuthStorePwd,
                SecretsManagerService.SslContextType.TLS,
                SecretsManagerService.KeyStoreType.PKCS12,
                bindingProvider);
    }

    private String getEndpoint(Node serviceNode) {
        Node versionsNode = getNodeWithTag(serviceNode, "Versions");

        if (versionsNode == null) {
            throw new IllegalArgumentException("No version tags found");
        }

        NodeList versionNodes = versionsNode.getChildNodes();

        for (int i = 0, n = versionNodes.getLength(); i < n; ++i) {
            Node endpointNode = getNodeWithTag(versionNodes.item(i), "EndpointTLS");

            if (endpointNode == null || !endpointNode.hasAttributes()
                    || endpointNode.getAttributes().getNamedItem("Location") == null) {
                continue;
            }

            String location = endpointNode.getAttributes().getNamedItem("Location").getTextContent();

            if (location.startsWith(connectorBaseUri)) {
                return location;
            }
        }

        throw new IllegalArgumentException("Invalid service node");
    }

    private static Node getNodeWithTag(Node node, String tagName) {
        NodeList nodeList = node.getChildNodes();

        for (int i = 0, n = nodeList.getLength(); i < n; ++i) {
            Node childNode = nodeList.item(i);

            // ignore namespace entirely
            if (tagName.equals(childNode.getNodeName()) || childNode.getNodeName().endsWith(":" + tagName)) {
                return childNode;
            }
        }

        return null;
    }
}
