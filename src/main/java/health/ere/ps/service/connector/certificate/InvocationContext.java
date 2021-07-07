package health.ere.ps.service.connector.certificate;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;

/**
 * Represents a calling context
 */
public class InvocationContext {

    private String mandantId;
    private String clientSystemId;
    private String workplaceId;

    public InvocationContext() {
    }

    /**
     * Constructor
     *
     * @param mandantId      client Id
     * @param clientSystemId client system
     * @param workplaceId    work place
     */
    public InvocationContext(String mandantId, String clientSystemId, String workplaceId) {
        this.mandantId = mandantId;
        this.clientSystemId = clientSystemId;
        this.workplaceId = workplaceId;
    }

    /**
     * Converts the call context into the representation required for the SOAP interface.
     *
     * @return Call context in the representation required for the SOAP interface.
     */
    public ContextType convertToContextType() {
        ContextType contextType = new ContextType();
        contextType.setMandantId(mandantId);
        contextType.setClientSystemId(clientSystemId);
        contextType.setWorkplaceId(workplaceId);
        return contextType;
    }
}
