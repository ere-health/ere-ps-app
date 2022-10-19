package health.ere.ps.model.idp.client.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.jupiter.api.Test;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.idp.client.authentication.IdpJwtProcessor;
import health.ere.ps.model.idp.client.authentication.JwtBuilder;
import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolAlgorithmSuiteIdentifiers;
import health.ere.ps.model.idp.client.data.IdpKeyDescriptor;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.profile.TitusTestProfile;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.idp.crypto.X509ClaimExtraction;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;

@TestProfile(TitusTestProfile.class)
public class SSOTokenBuilderTest {

	@Inject
	ConnectorCardsService connectorCardsService;

	@Inject
	CardCertificateReaderService cardCertificateReaderService;

	@InjectMock
	SsoTokenBuilder builder;

	@Inject
	RuntimeConfig config;

	@Inject
	IdpJwtProcessor jwtProcessor;

	@Inject
	JwtBuilder jwtbuild;

	@Test
	public void buildSSOTokenTest() throws Exception {

		/*
		 * 1.Does not work ConnectorCardsService csd =
		 * mock(ConnectorCardsService.class); new ConnectorCardsService();
		 * 
		 * String cardHandle = csd.getConnectorCardHandle(
		 * ConnectorCardsService.CardHandleType.UNKNOWN, config);
		 * 
		 * X509Certificate x509Certificate =
		 * cardCertificateReaderService.retrieveSmcbCardCertificate(cardHandle, null);
		 */

		/*
		 * 2. Does not work CertificateFactory certFactory =
		 * CertificateFactory.getInstance("X.509", BOUNCY_CASTLE_PROVIDER); selfCert =
		 * (X509Certificate) certFactory .generateCertificate(new URL("" +
		 * "/VAUCertificate").openStream()); File keystoreFileName = new
		 * File("src/test/resources/certs/keystore.p12"); try (InputStream in = new
		 * FileInputStream(keystoreFileName)) { selfCert = (X509Certificate)
		 * certFactory.generateCertificate(in); }
		 */
		
		/* 3. worked method : Certificate generation */
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		KeyPair keypair = keyGen.generateKeyPair();
		RSAPrivateKey privKey = (RSAPrivateKey) keypair.getPrivate();
		RSAPublicKey pubKey = (RSAPublicKey) keypair.getPublic();
	
		
		ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("secp256r1");
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
		keyPairGenerator.initialize(ecGenSpec, new SecureRandom());
		java.security.KeyPair pair = keyPairGenerator.generateKeyPair();
		java.security.interfaces.ECPrivateKey privateKey = (java.security.interfaces.ECPrivateKey) pair.getPrivate();
		ECPublicKey publicKey = (ECPublicKey) pair.getPublic();
		
		
		X509Certificate selfCert = createCertificate("CN=Client", "CN=Client", pubKey, privKey);
		
	
		ZonedDateTime givenTime = ZonedDateTime.now();


		builder = mock(SsoTokenBuilder.class);
		IdpJwe buildSsoToken = builder.buildSsoToken(selfCert, givenTime);
		IdpJwe expectedToken =generateExpected(privateKey, publicKey, selfCert, givenTime);

	
		assertEquals(expectedToken, buildSsoToken);

	}

	private IdpJwe generateExpected(PrivateKey privKey, PublicKey pubKey, X509Certificate selfCert, ZonedDateTime givenTime) {
	
		IdpJwe encrypt ;
		final Map<String, Object> bodyClaimsMap = new HashMap<>();
		final Map<String, Object> headerClaimsMap = new HashMap<>();
		headerClaimsMap.put(ClaimName.ALGORITHM.getJoseName(),
				BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256);
		bodyClaimsMap.put(ClaimName.CONFIRMATION.getJoseName(),
				IdpKeyDescriptor.constructFromX509Certificate(selfCert));
		headerClaimsMap.put(ClaimName.TYPE.getJoseName(), "JWT");
		bodyClaimsMap.put(ClaimName.ISSUER.getJoseName(), "www.example.com");
		bodyClaimsMap.put(ClaimName.ISSUED_AT.getJoseName(), givenTime.toEpochSecond());
		bodyClaimsMap.put(ClaimName.AUTH_TIME.getJoseName(), givenTime.toEpochSecond());
		bodyClaimsMap.putAll(X509ClaimExtraction.extractClaimsFromCertificate(selfCert));
		JwtBuilder build = new JwtBuilder(privKey,selfCert,false);
		build.addAllHeaderClaims(headerClaimsMap);
		build.addAllBodyClaims(bodyClaimsMap);
		build.expiresAt(givenTime.plusHours(12));
		
		try
		{
		IdpJwtProcessor jwtproc = new IdpJwtProcessor(selfCert);
		jwtProcessor = mock(IdpJwtProcessor.class);
		when(jwtProcessor.buildJwt(build)).thenReturn(build.buildJwt());
		encrypt = jwtproc.buildJwt(build).encrypt(pubKey);
		}
		catch(Exception e)
		{
			System.out.println("Could not generate Token");
			return null;
		}
	
		return encrypt;
	}

	private static X509Certificate createCertificate(String dn, String issuer, java.security.PublicKey publicKey,
			PrivateKey privateKey) throws Exception {
		X509V3CertificateGenerator certGenerator = new X509V3CertificateGenerator();
		certGenerator.setSerialNumber(BigInteger.valueOf(Math.abs(new Random().nextLong())));
		certGenerator.setIssuerDN(new X509Name(dn));
		certGenerator.setSubjectDN(new X509Name(dn));
		certGenerator.setIssuerDN(new X509Name(issuer)); // Set issuer!
		certGenerator.setNotBefore(Calendar.getInstance().getTime());
		certGenerator.setNotAfter(Calendar.getInstance().getTime());
		certGenerator.setSignatureAlgorithm("SHA1withRSA");
		certGenerator.setPublicKey(publicKey);
		X509Certificate certificate = (X509Certificate) certGenerator.generate(privateKey);
		return certificate;
		
		
	}

}
