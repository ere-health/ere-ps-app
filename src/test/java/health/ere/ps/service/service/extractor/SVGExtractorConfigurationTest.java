package health.ere.ps.service.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class SVGExtractorConfigurationTest {

    @Test
    public void testConstructorWithMuster16Template() {
        // Arrange
        String name = "TestConfig";
        float xOffset = 1.0f;
        float yOffset = 2.0f;
        float scale = 0.5f;
        int rotateDegree = 90;
        String muster16Template = "TemplateString";

        // Act
        SVGExtractorConfiguration config = new SVGExtractorConfiguration(
                name, xOffset, yOffset, scale, rotateDegree, muster16Template
        );

        // Assert
        assertEquals(name, config.NAME);
        assertEquals(xOffset, config.X_OFFSET, 0.001f); // Using delta for float comparison
        assertEquals(yOffset, config.Y_OFFSET, 0.001f);
        assertEquals(scale, config.SCALE, 0.001f);
        assertEquals(rotateDegree, config.ROTATE_DEGREE);
        assertEquals(muster16Template, config.MUSTER_16_TEMPLATE);
    }

    @Test
    public void testConstructorWithoutMuster16Template() {
        // Arrange
        String name = "TestConfig";
        float xOffset = 1.0f;
        float yOffset = 2.0f;
        float scale = 0.5f;
        int rotateDegree = 90;

        // Act
        SVGExtractorConfiguration config = new SVGExtractorConfiguration(
                name, xOffset, yOffset, scale, rotateDegree
        );

        // Assert
        assertEquals(name, config.NAME);
        assertEquals(xOffset, config.X_OFFSET, 0.001f); // Using delta for float comparison
        assertEquals(yOffset, config.Y_OFFSET, 0.001f);
        assertEquals(scale, config.SCALE, 0.001f);
        assertEquals(rotateDegree, config.ROTATE_DEGREE);
        assertNull(config.MUSTER_16_TEMPLATE);
    }
}
