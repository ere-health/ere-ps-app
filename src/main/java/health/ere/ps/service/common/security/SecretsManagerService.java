package health.ere.ps.service.common.security;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.BERTags;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.checkerframework.checker.units.qual.A;
import org.jose4j.base64url.Base64Url;
import org.jose4j.keys.X509Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.service.idp.crypto.CryptoLoader;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

@ApplicationScoped
public class SecretsManagerService {

    private static Logger log = Logger.getLogger(SecretsManagerService.class.getName());

    public enum SslContextType {
        SSL("SSL"), TLS("TLS");

        SslContextType(String sslContextType) {
            this.sslContextType = sslContextType;
        }

        private String sslContextType;

        public String getSslContextType() {
            return sslContextType;
        }
    }

    public enum KeyStoreType {
        JKS("jks"), PKCS12("pkcs12");

        KeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        private String keyStoreType;

        public String getKeyStoreType() {
            return keyStoreType;
        }
    }

    public SecretsManagerService() {

    }

    public KeyStore createTrustStore(String trustStoreFilePath,
                               KeyStoreType keyStoreType,
                               char[] keyStorePassword)
            throws SecretsManagerException {
        KeyStore ks;

        try {
            ks = KeyStore.getInstance(keyStoreType.getKeyStoreType());

            ks.load(null, keyStorePassword);

            Path tsFile = Paths.get(trustStoreFilePath);

            if(!Files.exists(tsFile)) {
                if(tsFile.toFile().getParentFile() != null) {
                    tsFile.toFile().getParentFile().mkdirs();
                    tsFile.toFile().createNewFile();
                }
            }

            try(FileOutputStream trustStoreOutputStream = new FileOutputStream(trustStoreFilePath)) {
                ks.store(trustStoreOutputStream, keyStorePassword);
            }
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new SecretsManagerException("Error creating trust store.", e);
        }

        return ks;
    }

    public KeyStore saveTrustedCertificate(String trustStoreFilePath, char[] keyStorePassword,
                                           String certificateAlias,
                                       Certificate certificate) throws CertificateException,
            KeyStoreException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = initializeTrustStoreFromFile(trustStoreFilePath,
                keyStorePassword);

        trustStore.setCertificateEntry(certificateAlias, certificate);

        try(FileOutputStream trustStoreOutputStream = new FileOutputStream(trustStoreFilePath)) {
            trustStore.store(trustStoreOutputStream, keyStorePassword);
        }

        return trustStore;
    }

    public KeyStore saveTrustedCertificate(String trustStoreFilePath, char[] keyStorePassword,
                                           String certificateAlias,
                                         byte[] certBytes) throws SecretsManagerException {
        try {
                X509Certificate x509Certificate;
//                String certB64 = Base64Url.decodeToUtf8String(new String(certBytes));
            String certB64 = "MIIE+jCCA+KgAwIBAgIHAgHpEyMBmzANBgkqhkiG9w0BAQsFADCBmjELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxSDBGBgNVBAsMP0luc3RpdHV0aW9uIGRlcyBHZXN1bmRoZWl0c3dlc2Vucy1DQSBkZXIgVGVsZW1hdGlraW5mcmFzdHJ1a3R1cjEgMB4GA1UEAwwXR0VNLlNNQ0ItQ0EyNCBURVNULU9OTFkwHhcNMjAxMTI3MDAwMDAwWhcNMjMxMTE2MDAwMDAwWjCBhDELMAkGA1UEBhMCREUxHDAaBgNVBAoMEzIwMjExMDEyMiBOT1QtVkFMSUQxEjAQBgNVBAQMCURyb21idXNjaDERMA8GA1UEKgwIV2FsdHJhdXQxMDAuBgNVBAMMJ0FyenRwcmF4aXMgV2FsdHJhdXQgRHJvbWJ1c2NoIFRFU1QtT05MWTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMEI0VNeP25qjKue36MFmSWMtZsxv197JdhxV75Q4AMq/FK67yqku8sQF9OmhYloaeuziNkNSTHd+8fAIqsbSxQYRwobOhweciOBknbVIALJRtAkYPJJBxahh4zHPLbEzDLWYhnVV7Wy60d4wZF/f94cHm9qBlhWiDmqXCObUv+alscCxST2Ll5MFCe4Iz2CNP+5LzX4jfBnGVWYooQJKwKPKpIhyAFUHywHuaZ/KfpIHPlyXq8vuMJ5ZCPDfW7n8GFRB2Dx1MGOWiJMa63f5DBjF0ozZSxVyof3A4z22sJ+YG0jYfuyhVRwsSciycWFxh9CwtcNkfzfhQG3AHa5Z8ECAwEAAaOCAVcwggFTMAwGA1UdEwEB/wQCMAAwHwYDVR0jBBgwFoAUeunhb+oUWRYF7gPp0/0hq97p2Z4wHQYDVR0OBBYEFJFDx5Q9Gonx3vTQ/Mg/vb4FVsXLMCwGA1UdHwQlMCMwIaAfoB2GG2h0dHA6Ly9laGNhLmdlbWF0aWsuZGUvY3JsLzA4BggrBgEFBQcBAQQsMCowKAYIKwYBBQUHMAGGHGh0dHA6Ly9laGNhLmdlbWF0aWsuZGUvb2NzcC8wEwYDVR0lBAwwCgYIKwYBBQUHAwIwVAYFKyQIAwMESzBJMEcwRTBDMEEwFgwUQmV0cmllYnNzdMOkdHRlIEFyenQwCQYHKoIUAEwEMhMcMS0yLUFSWlQtV2FsdHJhdXREcm9tYnVzY2gwMTAOBgNVHQ8BAf8EBAMCBaAwIAYDVR0gBBkwFzAKBggqghQATASBIzAJBgcqghQATARNMA0GCSqGSIb3DQEBCwUAA4IBAQBNVJKRDnooSI29jIG5O0G2nZbB82oigJh0FUgBwV1qOnbLhtx4+9u2QyBMI8ZZ+CUwxbGVRDMXnE5ONclgjAeolizhAFD1nAD91UhnXZJQZ84ZhkDVc2tulyVWoYiP3on/x0LD7hZlNhhAJzov+6cV4QbzIWahWMaBiWk3itKUwE7tu/jmS7+Y8UGbOo3L4A71F92CuJS2GIltySC3IdSYSKGW3I1O2/fkgTBlkv1Gv6ez3oNW90L4zrgUZ3Fr4XAzgegx95rn0b7TTt/N/xHBIWtSNDFS5gx1H4JREvQxF1I3sjxF9CvIeCTL6ZdS4FuV163cWL0Nu++tIe9GO+JJ";
                
                log.log(Level.INFO,
                        String.format("Byte array len = %d, String array size = %d, Base64 Bytes " +
                                        "= %s",
                                certBytes.length, certB64.length(),
                                certBytes));
                
                byte encodedCert[] = Base64.getDecoder().decode(certB64);

                try(InputStream is = new ByteArrayInputStream(encodedCert)) {
                    x509Certificate =
                            (X509Certificate) CertificateFactory.getInstance("X.509")
                                    .generateCertificate(is);
                }

                return saveTrustedCertificate(trustStoreFilePath, keyStorePassword,
                        certificateAlias, x509Certificate);
            } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
                throw new SecretsManagerException("Error saving certificate in trust store", e);
            }
    }

    public void deleteTrustStore(String trustStorePath) throws IOException {
        Files.delete(Paths.get(trustStorePath));
    }

    public KeyStore initializeTrustStoreFromFile(String trustStoreFilePath,
                                        char[] keyStorePassword)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance(new File(trustStoreFilePath), keyStorePassword);
        
        return trustStore;
    }

    public KeyStore initializeTrustStoreFromInputStream(InputStream trustStoreInputStream,
                                                        KeyStoreType keyStoreType,
                                                 char[] keyStorePassword)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance(keyStoreType.getKeyStoreType());
        trustStore.load(trustStoreInputStream, keyStorePassword);

        return trustStore;
    }

    public SSLContext createSSLContext(InputStream trustStoreInputStream, char[] keyStorePassword,
                                    SslContextType sslContextType, KeyStoreType keyStoreType)
            throws Exception {
        SSLContext sc = SSLContext.getInstance(sslContextType.getSslContextType());

        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );

        KeyStore ks = KeyStore.getInstance(keyStoreType.getKeyStoreType());
        ks.load(trustStoreInputStream, keyStorePassword);

        kmf.init(ks, keyStorePassword);

        sc.init( kmf.getKeyManagers(), null, null );

        return sc;
    }

    public static SSLContext setUpCustomSSLContext(InputStream p12Certificate) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            KeyStore ks = KeyStore.getInstance("PKCS12");
            // Download this file from the titus backend
            // https://frontend.titus.ti-dienste.de/#/platform/mandant
            ks.load(p12Certificate, "00".toCharArray());
            kmf.init(ks, "00".toCharArray());
            sc.init(kmf.getKeyManagers(), null, null);
            return sc;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException
                | UnrecoverableKeyException | KeyManagementException e) {
            log.log(Level.SEVERE, "Could not set up custom SSLContext", e);
            throw new RuntimeException(e);
        }
    }
}
