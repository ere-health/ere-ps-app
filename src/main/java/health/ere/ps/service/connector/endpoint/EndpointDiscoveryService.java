package health.ere.ps.service.connector.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import health.ere.ps.config.AppConfig;
import health.ere.ps.service.common.security.SecretsManagerService;

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
    @ConfigProperty(name = "card-service.endpoint.address")
    Optional<String> fallbackCardServiceEndpointAddress;

    @Inject
    AppConfig appConfig;
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

        if (!appConfig.getVerifyHostname().equals("true")) {
            // disable hostname verification
            // This line is currently not working
            clientBuilder = clientBuilder.hostnameVerifier(new SSLUtilities.FakeHostnameVerifier());
        }

        Invocation invocation = clientBuilder.build()
                .target(appConfig.getConnectorBaseURI())
                .path("/connector.sds")
                .request()
                .buildGet();

        try (InputStream inputStream = invocation.invoke(InputStream.class)) {
            Document document = DocumentBuilderFactory.newDefaultInstance()
                    .newDocumentBuilder()
                    .parse(inputStream);

            extractAndSetConnectorVersion(document);

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
                        signatureServiceEndpointAddress = getEndpoint(node, "PTV4+".equals(appConfig.getConnectorVersion()) ? "7.5.4" : null);
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

    private void extractAndSetConnectorVersion(Document document) {
        try {
            //Staging/probably prod as well
            Node productTypeNode = getNodeWithTag(getNodeWithTag(getNodeWithTag(document.getDocumentElement(),
                    "ProductInformation"), "ProductTypeInformation"), "ProductType");

            //Titus
            Node productNameNode = getNodeWithTag(getNodeWithTag(getNodeWithTag(document.getDocumentElement(),
                    "ProductInformation"), "ProductMiscellaneous"), "ProductName");

            String productType = productTypeNode.getTextContent();
            String productName = productNameNode.getTextContent();
            String versionContainingText = "";

            if (productType.contains("PTV")) {
                versionContainingText = productType;
            } else if (productName.contains("PTV")) {
                versionContainingText = productName;
            } else {
                log.warning("Could not find the version of the connector to use from connector.sds, " +
                        "using the one from the configuration:" + appConfig.getConnectorVersion());
            }

            if (versionContainingText.contains("PTV4+")) {
                log.info("Connector version PTV4+ found in connector.sds");
                appConfig.setConnectorVersion("PTV4+");
            } else if (versionContainingText.contains("PTV4")) {
                log.info("Connector version PTV4 found in connector.sds");
                appConfig.setConnectorVersion("PTV4");
            } else {
                log.warning("Could not determine the version of the connector to use from connector.sds, " +
                        "using the one from the configuration:" + appConfig.getConnectorVersion());
            }
        } catch (Exception e) {
            log.warning("Could not determine the version of the connector to use from connector.sds, " +
                    "using the one from the configuration:" + appConfig.getConnectorVersion());
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
        return getEndpoint(serviceNode, null);
    }

    private String getEndpoint(Node serviceNode, String version) {
        Node versionsNode = getNodeWithTag(serviceNode, "Versions");

        if (versionsNode == null) {
            throw new IllegalArgumentException("No version tags found");
        }
        NodeList versionNodes = versionsNode.getChildNodes();

        for (int i = 0, n = versionNodes.getLength(); i < n; ++i) {
            Node versionNode = versionNodes.item(i);

            // if we have a specified version search in the list until we find it
            if(version != null && versionNode.hasAttributes() && !version.equals(versionNode.getAttributes().getNamedItem("Version").getTextContent())) {
                continue;
            }

            Node endpointNode = getNodeWithTag(versionNode, "EndpointTLS");

            if (endpointNode == null || !endpointNode.hasAttributes()
                    || endpointNode.getAttributes().getNamedItem("Location") == null) {
                continue;
            }

            String location = endpointNode.getAttributes().getNamedItem("Location").getTextContent();
            if (location.startsWith(appConfig.getConnectorBaseURI())) {
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
