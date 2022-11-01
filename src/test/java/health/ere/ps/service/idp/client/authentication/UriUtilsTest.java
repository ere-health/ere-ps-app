package health.ere.ps.service.idp.client.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import health.ere.ps.exception.idp.IdpException;

public class UriUtilsTest {
  @Test
  public void ParseDifferentURIParameters() throws IdpException  {

      final String testUriString = "https://www.google.com?hello=Pablo&bye=2";

      final Map<String, String> testExtractParameterMap = UriUtils.extractParameterMap(testUriString);

      assertEquals(2, testExtractParameterMap.size(), "The URL should parse 2 parameters");
      
      assertEquals("Pablo", testExtractParameterMap.get("hello"), 
        "System should correctly parse the first parameter");

  }
}
