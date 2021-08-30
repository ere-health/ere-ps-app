package health.ere.ps.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.LogManager;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JavaScriptTest {

    public static String utilJavaScript = "var print = function (s) { __newOut.print(s); }; var println = function (s) { __newOut.println(s); };";

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
    public void test() throws ScriptException, IOException {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(8192);
        PrintStream newOut = new PrintStream(outputBuffer, true);
        Bindings bindings = jsEngine.createBindings();
        bindings.put("__newOut", newOut);
        String js = new String(Files.readAllBytes(Paths.get("src/test/resources/javascript/create-e-prescription.js")));
        jsEngine.eval(utilJavaScript + js, bindings);
        newOut.close();
        String returnString = outputBuffer.toString();
        System.out.println(returnString);
        outputBuffer.close();
    }  
}
