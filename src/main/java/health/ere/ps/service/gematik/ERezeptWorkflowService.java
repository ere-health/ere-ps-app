package health.ere.ps.service.gematik;

import java.io.StringReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class ERezeptWorkflowService {

    FhirContext fhirContext = FhirContext.forR4();

    /**
     * A typical muster 16 form can contain up to 3 e prescriptions
     * This function has to be called multiple times
     * 
     * This function takes a bundle e.g.
     * https://github.com/ere-health/ere-ps-app/blob/main/src/test/resources/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml
     * @param bundle
     * @return
     */
    public Bundle createERezeptOnPresciptionServer(Bundle bundle) {
        // Example: src/test/resources/gematik/Task-4711.xml
        Task task = createERezeptTask();

        // Example: src/test/resources/gematik/Bundle-4fe2013d-ae94-441a-a1b1-78236ae65680.xml
        updateBundleWithTask(task, bundle);

        // TODO: use correct object
        Object signedDocument = signBundleWithIdentifiers(bundle);

        updateERezeptTask(task, signedDocument);

        return bundle;
    }

    /**
     * This function adds the E-Rezept to the previously created
     * task.
     * 
     * @param task
     * @param signedDocument
     */
    public void updateERezeptTask(Task task, Object signedDocument) {
    }

    /**
     * Adds the identifiers to the bundle.
     * 
     * @param task
     * @param bundle
     */
    public void updateBundleWithTask(Task task, Bundle bundle) {
    }

    /**
     * This function signs the bundle with the SignService from the connector.
     * @return
     */
    public void signBundleWithIdentifiers(Bundle bundle) {

    }

    /**
     * This function creates an empty task based on workflow 160 (Muster 16)
     * on the prescription server.
     * 
     * @return
     */
    private Task createERezeptTask() {
        
        // https://github.com/gematik/api-erp/blob/master/docs/erp_bereitstellen.adoc#e-rezept-erstellen
        // TODO: POST to https://prescriptionserver.telematik/Task/$create

        String parameters = "<Parameters xmlns=\"http://hl7.org/fhir\">\n"+
        "  <parameter>\n"+
        "    <name value=\"workflowType\"/>\n"+
        "    <valueCoding>\n"+
        "      <system value=\"https://gematik.de/fhir/CodeSystem/Flowtype\"/>\n"+
        "      <code value="160"/>\n"+
        "    </valueCoding>\n"+
        "  </parameter>\n"+
        "</Parameters>\n";

        String bearerToken = "TODO";
        Client client = ClientBuilder.newBuilder().build();
        String s = client.target("https://prescriptionserver.telematik/Task/$create").request().header("Authorization", "Bearer "+bearerToken).post(Entity.entity(parameters, "application/fhir+xml; charset=UTF-8")).getEntity(String.class);
        Task task = fhirContext.newXmlParser().parseResource(Task.class, new StringReader(s));
        return task;
    }
    
}
