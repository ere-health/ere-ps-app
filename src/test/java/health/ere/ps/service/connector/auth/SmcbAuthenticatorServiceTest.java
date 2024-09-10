package health.ere.ps.service.connector.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;

import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;

public class SmcbAuthenticatorServiceTest {
    @Test
    public void testConvert() throws JoseException {
        String base64urlString = "MEUCIGYFXpGblxpAvUE21td5u33ahar2wsRiIgG_cu49QujlAiEAgWqy4Hyw43mXxuZLlfKk9DNlmnNq9DtZ2SSYREZGf7g";
        byte[] signature = Base64.getUrlDecoder().decode(base64urlString);

        byte[] signatureBytes = SmcbAuthenticatorService.convertDerECDSAtoConcated(signature);
        assertEquals("ZgVekZuXGkC9QTbW13m7fdqFqvbCxGIiAb9y7j1C6OWBarLgfLDjeZfG5kuV8qT0M2Wac2r0O1nZJJhERkZ_uA==", Base64.getUrlEncoder().encodeToString(signatureBytes));
    }

    @Test
    public void testConvert2() throws JoseException {
        String base64urlString = "MEQCIB2LzMza9ecfq0pArrkRKmqIF3JLnDnLxyor/QflMytCAiAqykMJretmDiJbTk4/w0npgqEZO5LRrrM0nXUGLZ4HtQ==";
        byte[] signature = Base64.getDecoder().decode(base64urlString);

        byte[] signatureBytes = SmcbAuthenticatorService.convertDerECDSAtoConcated(signature);
        assertEquals("HYvMzNr15x-rSkCuuREqaogXckucOcvHKiv9B-UzK0IqykMJretmDiJbTk4_w0npgqEZO5LRrrM0nXUGLZ4HtQ==", Base64.getUrlEncoder().encodeToString(signatureBytes));
    }

    
}
