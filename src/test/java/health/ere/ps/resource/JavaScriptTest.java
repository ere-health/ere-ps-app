package health.ere.ps.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.LogManager;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// @Disabled
public class JavaScriptTest {

    public static String utilJavaScript = "var print = function (s) { java.lang.System.out.print(s); }; var println = function (s) { java.lang.System.out.println(s); };";

    @BeforeEach
    void init() {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                JavaScriptTest.class
                            .getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateBundle() throws ScriptException, IOException {
        runScript("src/test/resources/javascript/validate-bundle.js");
    }

    @Test
    public void testCreateEPrescription() throws ScriptException, IOException {
        runScript("src/test/resources/javascript/create-e-prescription.js");
    }

    @Test
    public void testCreateEPrescriptionXml() throws ScriptException, IOException {
        runScript("src/test/resources/javascript/create-e-prescription-from-xml.js");
    }
    
    @Test
    public void testGeneratePrintOut() throws ScriptException, IOException {
        runScript("src/test/resources/javascript/generate-print-out.js");
    }

    @Test
    public void testRuntimeConfig() throws ScriptException, IOException {
        runScript("src/test/resources/javascript/create-e-prescription-with-runtime-config.js");
    }

    private void runScript(String script) throws IOException, ScriptException {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
        Bindings bindings = jsEngine.createBindings();
        String js = new String(Files.readAllBytes(Paths.get(script)));
        jsEngine.eval(utilJavaScript + js, bindings);
    }
}
