package health.ere.ps.service.connector.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;

import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticate.OptionalInputs;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import jakarta.xml.ws.Holder;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;

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

    @Test
    public void testECC() throws JoseException, FaultMessage {
        SmcbAuthenticatorService smcbAuthenticatorService = new SmcbAuthenticatorService();
        AuthSignatureServicePortType authSignatureServicePortType = mock(AuthSignatureServicePortType.class);
        smcbAuthenticatorService.connectorServicesProvider = mock(MultiConnectorServicesProvider.class);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Holder<SignatureObject> holder = (Holder) args[5];
                holder.value = new SignatureObject();
                Base64Signature base64Signature = new Base64Signature();
                String base64urlString = "MEQCIB2LzMza9ecfq0pArrkRKmqIF3JLnDnLxyor/QflMytCAiAqykMJretmDiJbTk4/w0npgqEZO5LRrrM0nXUGLZ4HtQ==";
                byte[] signature = Base64.getDecoder().decode(base64urlString);
                base64Signature.setValue(signature);
                holder.value.setBase64Signature(base64Signature);
                return null;
            }
        }).when(authSignatureServicePortType).externalAuthenticate(any(), any(), any(), any(), any(), any());
        when(smcbAuthenticatorService.connectorServicesProvider.getAuthSignatureServicePortType(any())).thenReturn(authSignatureServicePortType);
        
        byte[] signatureBytes = smcbAuthenticatorService.externalAuthenticate(null, null, null, true);

        assertEquals("HYvMzNr15x-rSkCuuREqaogXckucOcvHKiv9B-UzK0IqykMJretmDiJbTk4_w0npgqEZO5LRrrM0nXUGLZ4HtQ==", Base64.getUrlEncoder().encodeToString(signatureBytes));

        signatureBytes = smcbAuthenticatorService.externalAuthenticate(null, null, null, false);

        assertEquals("MEQCIB2LzMza9ecfq0pArrkRKmqIF3JLnDnLxyor_QflMytCAiAqykMJretmDiJbTk4_w0npgqEZO5LRrrM0nXUGLZ4HtQ==", Base64.getUrlEncoder().encodeToString(signatureBytes));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<OptionalInputs> signatureObjectCaptor = ArgumentCaptor.forClass(OptionalInputs.class);
        verify(authSignatureServicePortType, times(2)).externalAuthenticate(any(), any(), signatureObjectCaptor.capture(), any(), any(), any());
        assertEquals("urn:bsi:tr:03111:ecdsa", signatureObjectCaptor.getAllValues().get(0).getSignatureType());
        assertEquals("urn:ietf:rfc:3447", signatureObjectCaptor.getAllValues().get(1).getSignatureType());

    }

    
}
