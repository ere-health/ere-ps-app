package health.ere.ps.jsonb;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.math.BigInteger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonNumber;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.model.Operation;
import com.hp.jipp.trans.IppPacketData;
import de.gematik.ws.tel.error.v2.Error;
import de.gematik.ws.tel.error.v2.Error.Trace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.profile.TitusTestProfile;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;



@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class ThrowableAdapterTest {
	
	ThrowableAdapter throwableAdapter = new ThrowableAdapter();
	
	static final BigInteger ERROR_CODE = BigInteger.valueOf(4085);
	static Error faultInfo;
	static Trace trace;
	static StringWriter sw;
	static PrintWriter pw;
	
	@BeforeAll
	public static void init() {
		faultInfo 	= new Error();
		trace 		= new Trace();
		
		trace.setCode(ERROR_CODE);
		faultInfo.getTrace().add(trace);
	}
	@BeforeEach
	public void setUp() {
		sw 			= new StringWriter();
		pw  		= new PrintWriter(sw);
	}
	
	@Test
    public void testAuthSignatureServiceErroreCode(){
		
		String message 		= "Auth Signature Service Exception";
		String className 	= "de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage";
		
		de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage faultMessage =
				new de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage(message,faultInfo);
	    faultMessage.printStackTrace(pw);
        
       
        JsonObject jsonObject = throwableAdapter.adaptToJson(faultMessage);

        assertEquals(message,((JsonString) jsonObject.get("message")).getString());
    	assertEquals(className,((JsonString) jsonObject.get("class")).getString());
    	assertEquals(ERROR_CODE,((JsonNumber) jsonObject.get("errorCode")).bigIntegerValue());
    	assertEquals(sw.toString(),((JsonString) jsonObject.get("stacktrace")).getString());

    }
	
	@Test
    public void testSignatureServiceErroreCode(){
		
		String message 		= "Signature Service Exception";
		String className 	= "de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage";
		
		de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage faultMessage = 
				new de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage(message,faultInfo);
		faultMessage.printStackTrace(pw);
        
        
		JsonObject jsonObject = throwableAdapter.adaptToJson(faultMessage);
		
		assertEquals(message,((JsonString) jsonObject.get("message")).getString());
    	assertEquals(className,((JsonString) jsonObject.get("class")).getString());
    	assertEquals(ERROR_CODE,((JsonNumber) jsonObject.get("errorCode")).bigIntegerValue());
    	assertEquals(sw.toString(),((JsonString) jsonObject.get("stacktrace")).getString());
		
		
    }
	@Test
    public void testEventServiceErroreCode(){
		
		String message 		= "Event Service Exception";
		String className 	= "de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage";
		
		de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage faultMessage = 
				new de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage(message,faultInfo);
		faultMessage.printStackTrace(pw);
        
        
		JsonObject jsonObject = throwableAdapter.adaptToJson(faultMessage);
		
		assertEquals(message,((JsonString) jsonObject.get("message")).getString());
    	assertEquals(className,((JsonString) jsonObject.get("class")).getString());
    	assertEquals(ERROR_CODE,((JsonNumber) jsonObject.get("errorCode")).bigIntegerValue());
    	assertEquals(sw.toString(),((JsonString) jsonObject.get("stacktrace")).getString());
		
		
    }
	@Test
    public void testCertificateServiceErroreCode(){
		
		String message 		= "Certificate Service Exception";
		String className 	= "de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage";
		
		de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage faultMessage 
				= new de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage(message,faultInfo);
		faultMessage.printStackTrace(pw);
        
        
		JsonObject jsonObject = throwableAdapter.adaptToJson(faultMessage);
		
		assertEquals(message,((JsonString) jsonObject.get("message")).getString());
    	assertEquals(className,((JsonString) jsonObject.get("class")).getString());
    	assertEquals(ERROR_CODE,((JsonNumber) jsonObject.get("errorCode")).bigIntegerValue());
    	assertEquals(sw.toString(),((JsonString) jsonObject.get("stacktrace")).getString());
		
		
    }
	@Test
    public void testCardServiceErroreCode(){
		
		String message 		= "Card Service Exception";
		String className 	= "de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage";
		
		de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage faultMessage = 
				new de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage("Card Service Exception",faultInfo);
		faultMessage.printStackTrace(pw);
        
        
		JsonObject jsonObject = throwableAdapter.adaptToJson(faultMessage);
		
		assertEquals(message,((JsonString) jsonObject.get("message")).getString());
    	assertEquals(className,((JsonString) jsonObject.get("class")).getString());
    	assertEquals(ERROR_CODE,((JsonNumber) jsonObject.get("errorCode")).bigIntegerValue());
    	assertEquals(sw.toString(),((JsonString) jsonObject.get("stacktrace")).getString());
    }
	@Test
    public void testErrorCode0(){
		
		String message 		= "Error Code 0";
		String className 	= "java.lang.Exception";
		
		Exception exception = new Exception(message);
		exception.printStackTrace(pw);
          
        JsonObject jsonObject = throwableAdapter.adaptToJson(exception);

        assertEquals(message,((JsonString) jsonObject.get("message")).getString());
    	assertEquals(className,((JsonString) jsonObject.get("class")).getString());
    	assertEquals(BigInteger.valueOf(0),((JsonNumber) jsonObject.get("errorCode")).bigIntegerValue());
    	assertEquals(sw.toString(),((JsonString) jsonObject.get("stacktrace")).getString());

    }
	@Test
    public void testAdaptFromJson(){
		
		String message = "Error message";
		
		JsonObject jsonObject = Json.createObjectBuilder()
        .add("message", message)
        .build();
        
		Throwable t = throwableAdapter.adaptFromJson(jsonObject);
		
		assertEquals(message,t.getMessage());
    }
	
}
