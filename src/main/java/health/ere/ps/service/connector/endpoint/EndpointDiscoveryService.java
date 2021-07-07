package health.ere.ps.service.connector.endpoint;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureService;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import health.ere.ps.config.AppConfig;
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
    private static final Logger log = Logger.getLogger(EndpointDiscoveryService.class.getName());

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

    @ConfigProperty(name = "card-service.endpoint.address")
    Optional<String> fallbackCardServiceEndpointAddress;

    private String cardServiceEndpointAddress;

    @Inject
    SecretsManagerService secretsManagerService;

    private AuthSignatureServicePortType authSignatureService;

    @Inject
    AppConfig appConfig;


    @PostConstruct
    void obtainConfiguration() throws IOException, ParserConfigurationException, SecretsManagerException {
        // code copied from IdpClient.java

        authSignatureService = new AuthSignatureService(getClass().getResource("/AuthSignatureService_v7_4_1.wsdl")).getAuthSignatureServicePort();
        BindingProvider bp = (BindingProvider) authSignatureService;
        SSLContext sslContext;
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        try (FileInputStream fileInputStream = new FileInputStream(appConfig.getConnectorCertAuthStoreFile())) {
            sslContext = secretsManagerService.createSSLContext(fileInputStream,
                    appConfig.getConnectorCertAuthStoreFilePwd().toCharArray(),
                    SecretsManagerService.SslContextType.TLS,
                    SecretsManagerService.KeyStoreType.PKCS12,
                    bp);
            clientBuilder.sslContext(sslContext);
        } catch (IOException e) {
            log.severe("SSL transport configuration error.");
            // throw new SecretsManagerException("SSL transport configuration error.", e);
        }

        if (!isConnectorVerifyHostnames()) {
            // disable hostname verification
            // This line is currently not working
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
                    case "CardService": {
                        cardServiceEndpointAddress = getEndpoint(node);
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
            log.log(Level.SEVERE, "Could not parse connector.sds", e);
        }

        if (authSignatureServiceEndpointAddress == null) {
            authSignatureServiceEndpointAddress = fallbackAuthSignatureServiceEndpointAddress.orElseThrow();
        }
        if (cardServiceEndpointAddress == null) {
            cardServiceEndpointAddress = fallbackCardServiceEndpointAddress.orElseThrow();
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

    public String getCardServiceEndpointAddress() {
        return cardServiceEndpointAddress;
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
                appConfig.getConnectorCertAuthStoreFile(),
                appConfig.getConnectorCertAuthStoreFilePwd(),
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
