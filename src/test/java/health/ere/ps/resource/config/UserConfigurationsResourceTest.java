package health.ere.ps.resource.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class UserConfigurationsResourceTest {
  //Test GET request status code
  @Test
  public void testGetConfig() {
    given()
        .header("Content-Type", "application/json")
        .when()
        .get("http://localhost:8080/config")
        .then()
        .statusCode(200)
        .log()
        .all();
  }

  // Testing HTTP Error Codes
  @Test
  public void testGetConfigErrorNotFound() {
    RestAssured.baseURI = "http://localhost:8080/config";
    RequestSpecification httpRequest = RestAssured.given();
    Response response = httpRequest.request(Method.GET, "/1");
    int statusCode = response.getStatusCode();
    assertEquals(statusCode, 404, "Correct status code is not returned");
  }

  @Test
  public void testPutConfigNoHeaderError() {

    RestAssured.baseURI = "http://localhost:8080/config";
    RequestSpecification httpRequest = RestAssured.given();
    Response response = httpRequest.request(Method.PUT, "/");
    int statusCode = response.getStatusCode();
    assertEquals(statusCode, 415, "Correct status code is not returned");

  }

  @Test
  public void testPutConfigIsNull() {       
    given()
        .when()
        .header("X-erp-user","l")
        .header("Content-Type", "application/json")
        .header("Host", "erp.zentral.erp.splitdns.ti-dienste.de")
        .header("Authorization", "Bearer eyJraWQ.ewogImL2pA10Qql22ddtutrvx4FsDlz.rHQjEmB1lLmpqn9J")
        .header("User-Agent", "E-Rezept FdV 1.0.0")
        .header("Accept", "application/fhir+json;charset=utf-8")
        .put("http://localhost:8080/config")
        .then()
        .statusCode(500)
        .log()
        .all();
  }
}