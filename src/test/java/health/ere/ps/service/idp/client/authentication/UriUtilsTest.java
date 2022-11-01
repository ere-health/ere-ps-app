package health.ere.ps.service.idp.client.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import health.ere.ps.exception.idp.IdpException;

public class UriUtilsTest {
  @Test
  public void myTest() throws IdpException  {
      final String testUriString = "https://www.google.com?hello=Pablo&bye=2";

      final Map<String, String> getValues = UriUtils.extractParameterMap(testUriString) ;

      assertEquals(2, getValues.size(), "The URL should parse 2 parameters");
      
      assertEquals("Pablo", getValues.get("hello"), 
        "System should correctly parse the first parameter");

  }
}
