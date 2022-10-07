package health.ere.ps.resource.config;

import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.junit.jupiter.api.Test;

import health.ere.ps.model.config.UserConfigurations;
import io.quarkus.test.junit.mockito.InjectMock;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class UserConfigurationsResourceTest {
    
    @Test
    public void testGetConfigEndpoint() {
        given()
          .when().get("http://localhost:8080/config")
          .then()
             .statusCode(200);

             
    }
    @Test
    public void testPutConfigNullEndpoint() {
        UserConfigurations userConfiguration = new UserConfigurations();
        given()
          .when().put("http://localhost:8080/config")
          .then()
             .statusCode(415);
            //  .body(is("hello"));
    }
}