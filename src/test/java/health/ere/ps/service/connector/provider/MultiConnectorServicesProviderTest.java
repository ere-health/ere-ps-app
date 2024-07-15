package health.ere.ps.service.connector.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV740;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV755;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import health.ere.ps.config.SimpleUserConfig;
import health.ere.ps.config.UserConfig;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import health.ere.ps.config.RuntimeConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MultiConnectorServicesProviderTest {

    @Mock
    DefaultConnectorServicesProvider defaultConnectorServicesProvider;

    @Mock
    Event<Exception> eventException;

    @InjectMocks
    MultiConnectorServicesProvider multiConnectorServicesProvider;

    private UserConfig userConfig;
    private SimpleUserConfig simpleUserConfig;

    @BeforeEach
    void setUp() {
        userConfig = new UserConfig();
        simpleUserConfig = new SimpleUserConfig(userConfig);
        multiConnectorServicesProvider.singleConnectorServicesProvider = Collections.synchronizedMap(new HashMap<>());
    }

    @Test
    void testGetCardServicePortType() {
        SingleConnectorServicesProvider singleConnectorServicesProvider = mock(SingleConnectorServicesProvider.class);
        CardServicePortType cardServicePortType = mock(CardServicePortType.class);
        when(singleConnectorServicesProvider.getCardServicePortType()).thenReturn(cardServicePortType);
        multiConnectorServicesProvider.singleConnectorServicesProvider.put(simpleUserConfig, singleConnectorServicesProvider);

        CardServicePortType resultedCardServicePortType = multiConnectorServicesProvider.getCardServicePortType(userConfig);
        assertEquals(cardServicePortType, resultedCardServicePortType);
    }

    @Test
    void testGetSingleConnectorServicesProviderWithNullConfig() {
        assertEquals(defaultConnectorServicesProvider, multiConnectorServicesProvider.getSingleConnectorServicesProvider(null));
    }

    @Test
    void testGetSingleConnectorServicesProviderWithNewConfig() {
        assertNotNull(multiConnectorServicesProvider.getSingleConnectorServicesProvider(userConfig));
        assertTrue(multiConnectorServicesProvider.singleConnectorServicesProvider.containsKey(simpleUserConfig));
    }

    @Test
    void testGetSingleConnectorServicesProviderWithExistingConfig() {
        SingleConnectorServicesProvider singleConnectorServicesProvider = mock(SingleConnectorServicesProvider.class);
        multiConnectorServicesProvider.singleConnectorServicesProvider.put(simpleUserConfig, singleConnectorServicesProvider);

        assertEquals(singleConnectorServicesProvider, multiConnectorServicesProvider.getSingleConnectorServicesProvider(userConfig));
    }

    @Test
    void testGetCertificateServicePortType() {
        SingleConnectorServicesProvider singleConnectorServicesProvider = mock(SingleConnectorServicesProvider.class);
        CertificateServicePortType certificateServicePortType = mock(CertificateServicePortType.class);
        when(singleConnectorServicesProvider.getCertificateService()).thenReturn(certificateServicePortType);
        multiConnectorServicesProvider.singleConnectorServicesProvider.put(simpleUserConfig, singleConnectorServicesProvider);

        CertificateServicePortType resultedCertificateServicePortType = multiConnectorServicesProvider.getCertificateServicePortType(userConfig);
        assertEquals(certificateServicePortType, resultedCertificateServicePortType);
    }

    @Test
    void testGetEventServicePortType() {
        SingleConnectorServicesProvider singleConnectorServicesProvider = mock(SingleConnectorServicesProvider.class);
        EventServicePortType eventServicePortType = mock(EventServicePortType.class);
        when(singleConnectorServicesProvider.getEventServicePortType()).thenReturn(eventServicePortType);
        multiConnectorServicesProvider.singleConnectorServicesProvider.put(simpleUserConfig, singleConnectorServicesProvider);

        EventServicePortType resultedEventServicePortType = multiConnectorServicesProvider.getEventServicePortType(userConfig);
        assertEquals(eventServicePortType, resultedEventServicePortType);
    }

    @Test
    void testGetAuthSignatureServicePortType() {
        SingleConnectorServicesProvider singleConnectorServicesProvider = mock(SingleConnectorServicesProvider.class);
        AuthSignatureServicePortType authSignatureServicePortType = mock(AuthSignatureServicePortType.class);
        when(singleConnectorServicesProvider.getAuthSignatureServicePortType()).thenReturn(authSignatureServicePortType);
        multiConnectorServicesProvider.singleConnectorServicesProvider.put(simpleUserConfig, singleConnectorServicesProvider);

        AuthSignatureServicePortType resultedAuthSignatureServicePortType = multiConnectorServicesProvider.getAuthSignatureServicePortType(userConfig);
        assertEquals(authSignatureServicePortType, resultedAuthSignatureServicePortType);
    }

    @Test
    void testGetSignatureServicePortTypeV740() {
        SingleConnectorServicesProvider singleConnectorServicesProvider = mock(SingleConnectorServicesProvider.class);
        SignatureServicePortTypeV740 signatureServicePortTypeV740 = mock(SignatureServicePortTypeV740.class);
        when(singleConnectorServicesProvider.getSignatureServicePortType()).thenReturn(signatureServicePortTypeV740);
        multiConnectorServicesProvider.singleConnectorServicesProvider.put(simpleUserConfig, singleConnectorServicesProvider);

        SignatureServicePortTypeV740 resultedSignatureServicePortTypeV7401 = multiConnectorServicesProvider.getSignatureServicePortType(userConfig);
        assertEquals(signatureServicePortTypeV740, resultedSignatureServicePortTypeV7401);
    }

    @Test
    void testGetSignatureServicePortTypeV755WithMocks() {
        SingleConnectorServicesProvider singleConnectorServicesProvider = mock(SingleConnectorServicesProvider.class);
        SignatureServicePortTypeV755 signatureServicePortTypeV755 = mock(SignatureServicePortTypeV755.class);
        when(singleConnectorServicesProvider.getSignatureServicePortTypeV755()).thenReturn(signatureServicePortTypeV755);
        multiConnectorServicesProvider.singleConnectorServicesProvider.put(simpleUserConfig, singleConnectorServicesProvider);

        SignatureServicePortTypeV755 resultedSignatureServicePortTypeV7551 = multiConnectorServicesProvider.getSignatureServicePortTypeV755(userConfig);
        assertEquals(signatureServicePortTypeV755, resultedSignatureServicePortTypeV7551);
    }

    @Test
    void testGetVSDServicePortType() {
        SingleConnectorServicesProvider singleConnectorServicesProvider = mock(SingleConnectorServicesProvider.class);
        VSDServicePortType vsdServicePortType = mock(VSDServicePortType.class);
        when(singleConnectorServicesProvider.getVSDServicePortType()).thenReturn(vsdServicePortType);
        multiConnectorServicesProvider.singleConnectorServicesProvider.put(simpleUserConfig, singleConnectorServicesProvider);

        VSDServicePortType resultedVsdServicePortType = multiConnectorServicesProvider.getVSDServicePortType(userConfig);
        assertEquals(vsdServicePortType, resultedVsdServicePortType);
    }

    @Test
    void testGetContextTypeWithNullConfig() {
        ContextType contextType = mock(ContextType.class);
        when(defaultConnectorServicesProvider.getContextType()).thenReturn(contextType);

        ContextType resultedContextType = multiConnectorServicesProvider.getContextType(null);
        assertEquals(contextType, resultedContextType);
    }

    @Test
    void testGetContextTypeWithConfig() throws NoSuchFieldException, IllegalAccessException {

        setField(userConfig, "defaultMandantId", "mandantId");
        setField(userConfig, "defaultClientSystemId", "clientSystemId");
        setField(userConfig, "defaultWorkplaceId", "workplaceId");
        setField(userConfig, "defaultUserId", Optional.of("userId"));

        ContextType contextType = multiConnectorServicesProvider.getContextType(userConfig);
        assertEquals("mandantId", contextType.getMandantId());
        assertEquals("clientSystemId", contextType.getClientSystemId());
        assertEquals("workplaceId", contextType.getWorkplaceId());
        assertEquals("userId", contextType.getUserId());
    }

    @Test
    void testClearAll() {
        SingleConnectorServicesProvider singleProvider = mock(SingleConnectorServicesProvider.class);
        multiConnectorServicesProvider.singleConnectorServicesProvider.put(simpleUserConfig, singleProvider);
        assertFalse(multiConnectorServicesProvider.singleConnectorServicesProvider.isEmpty());

        multiConnectorServicesProvider.clearAll();
        assertTrue(multiConnectorServicesProvider.singleConnectorServicesProvider.isEmpty());
    }

    @Test
    void testGetSignatureServicePortTypeV755() {
        MultiConnectorServicesProvider multiConnectorServicesProvider = new MultiConnectorServicesProvider();
        RuntimeConfig runtimeConfig = new RuntimeConfig("eHBAHandle", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        runtimeConfig = new RuntimeConfig("eHBAHandle", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        assertEquals(1, multiConnectorServicesProvider.singleConnectorServicesProvider.size());

    }

    @Test
    void testGetSignatureServicePortTypeV7552() {
        MultiConnectorServicesProvider multiConnectorServicesProvider = new MultiConnectorServicesProvider();
        RuntimeConfig runtimeConfig = new RuntimeConfig("eHBAHandle", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        runtimeConfig = new RuntimeConfig("adasd", "SMCBHandle");
        multiConnectorServicesProvider.getSignatureServicePortTypeV755(runtimeConfig);
        assertEquals(2, multiConnectorServicesProvider.singleConnectorServicesProvider.size());

    }

    private void setField(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
