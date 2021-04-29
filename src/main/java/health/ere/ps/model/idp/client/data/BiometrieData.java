package health.ere.ps.model.idp.client.data;

import java.time.ZonedDateTime;

public class BiometrieData {
    private String id = "";
    private String idNumber = "anyIdNumber";
    private String keyIdentifier = "anyKeyIdentifier";
    private String signatureAlgorithm = "anySignatureAlgorithm";
    private String signature = "anySignature";
    private String authorityInfoAccess = "anyAuthorityInfoAccess";
    private String certId = "anyCertId";
    private String publicKey = "anyPublicKey";
    private String product = "anyProduct";
    private String deviceName = "anyDeviceName";
    private String keyDataAlgorithm = "anyKeyDataAlgorithm";
    private String keyData = "anyKeyData";
    private ZonedDateTime timestampPairing = ZonedDateTime.now();

    public BiometrieData(String id, String idNumber, String keyIdentifier, String signatureAlgorithm,
                         String signature, String authorityInfoAccess, String certId,
                         String publicKey, String product, String deviceName, String keyDataAlgorithm,
                         String keyData, ZonedDateTime timestampPairing) {
        this.id = id;
        this.idNumber = idNumber;
        this.keyIdentifier = keyIdentifier;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signature = signature;
        this.authorityInfoAccess = authorityInfoAccess;
        this.certId = certId;
        this.publicKey = publicKey;
        this.product = product;
        this.deviceName = deviceName;
        this.keyDataAlgorithm = keyDataAlgorithm;
        this.keyData = keyData;
        this.timestampPairing = timestampPairing;
    }

    public BiometrieData() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BiometrieData{");
        sb.append("id='").append(id).append('\'');
        sb.append(", idNumber='").append(idNumber).append('\'');
        sb.append(", keyIdentifier='").append(keyIdentifier).append('\'');
        sb.append(", signatureAlgorithm='").append(signatureAlgorithm).append('\'');
        sb.append(", signature='").append(signature).append('\'');
        sb.append(", authorityInfoAccess='").append(authorityInfoAccess).append('\'');
        sb.append(", certId='").append(certId).append('\'');
        sb.append(", publicKey='").append(publicKey).append('\'');
        sb.append(", product='").append(product).append('\'');
        sb.append(", deviceName='").append(deviceName).append('\'');
        sb.append(", keyDataAlgorithm='").append(keyDataAlgorithm).append('\'');
        sb.append(", keyData='").append(keyData).append('\'');
        sb.append(", timestampPairing=").append(timestampPairing);
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getKeyIdentifier() {
        return keyIdentifier;
    }

    public void setKeyIdentifier(String keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAuthorityInfoAccess() {
        return authorityInfoAccess;
    }

    public void setAuthorityInfoAccess(String authorityInfoAccess) {
        this.authorityInfoAccess = authorityInfoAccess;
    }

    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getKeyDataAlgorithm() {
        return keyDataAlgorithm;
    }

    public void setKeyDataAlgorithm(String keyDataAlgorithm) {
        this.keyDataAlgorithm = keyDataAlgorithm;
    }

    public String getKeyData() {
        return keyData;
    }

    public void setKeyData(String keyData) {
        this.keyData = keyData;
    }

    public ZonedDateTime getTimestampPairing() {
        return timestampPairing;
    }

    public void setTimestampPairing(ZonedDateTime timestampPairing) {
        this.timestampPairing = timestampPairing;
    }

    public static BiometrieDataBuilder builder() {
        return new BiometrieDataBuilder();
    }

    public static class BiometrieDataBuilder {
        private BiometrieData biometrieData;

        public BiometrieDataBuilder() {
            biometrieData = new BiometrieData();
        }

        public BiometrieDataBuilder id(String id) {
            biometrieData.setId(id);

            return this;
        }

        public BiometrieDataBuilder idNumber(String idNumber) {
            biometrieData.setIdNumber(idNumber);

            return this;
        }

        public BiometrieDataBuilder keyIdentifier(String keyIdentifier) {
            biometrieData.setKeyIdentifier(keyIdentifier);

            return this;
        }

        public BiometrieDataBuilder signatureAlgorithm(String signatureAlgorithm) {
            biometrieData.setSignatureAlgorithm(signatureAlgorithm);

            return this;
        }

        public BiometrieDataBuilder signature(String signature) {
            biometrieData.setSignature(signature);

            return this;
        }

        public BiometrieDataBuilder authorityInfoAccess(String authorityInfoAccess) {
            biometrieData.setAuthorityInfoAccess(authorityInfoAccess);

            return this;
        }

        public BiometrieDataBuilder certId(String certId) {
            biometrieData.setCertId(certId);

            return this;
        }

        public BiometrieDataBuilder publicKey(String publicKey) {
            biometrieData.setPublicKey(publicKey);

            return this;
        }

        public BiometrieDataBuilder product(String product) {
            biometrieData.setProduct(product);

            return this;
        }

        public BiometrieDataBuilder deviceName(String deviceName) {
            biometrieData.setDeviceName(deviceName);

            return this;
        }

        public BiometrieDataBuilder keyDataAlgorithm(String keyDataAlgorithm) {
            biometrieData.setKeyDataAlgorithm(keyDataAlgorithm);

            return this;
        }

        public BiometrieDataBuilder keyData(String keyData) {
            biometrieData.setKeyData(keyData);

            return this;
        }

        public BiometrieDataBuilder timestampPairing(ZonedDateTime timestampPairing) {
            biometrieData.setTimestampPairing(timestampPairing);

            return this;
        }
    }
}
