package health.ere.ps.service.connector.endpoint;

import health.ere.ps.service.common.security.SecretsManagerService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
    @ConfigProperty(name = "signature-service.endpoint.address")
    Optional<String> fallbackSignatureServiceEndpointAddress;
    @ConfigProperty(name = "certificate-service.endpoint.address")
    Optional<String> fallbackCertificateServiceEndpointAddress;
    @ConfigProperty(name = "event-service.endpoint.address")
    Optional<String> fallbackEventServiceEndpointAddress;
    @ConfigProperty(name = "connector.base-uri")
    String connectorBaseUri;
    @ConfigProperty(name = "connector.verify-hostname", defaultValue = "true")
    String connectorVerifyHostname;
    @ConfigProperty(name = "card-service.endpoint.address")
    Optional<String> fallbackCardServiceEndpointAddress;

    @Inject
    SecretsManagerService secretsManagerService;

    private String authSignatureServiceEndpointAddress;
    private String signatureServiceEndpointAddress;
    private String certificateServiceEndpointAddress;
    private String eventServiceEndpointAddress;
    private String cardServiceEndpointAddress;


    @PostConstruct
    void obtainConfiguration() throws IOException, ParserConfigurationException {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.sslContext(secretsManagerService.getSslContext());

        if (!connectorVerifyHostname.equals("true")) {
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

    private Node getNodeWithTag(Node node, String tagName) {
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
