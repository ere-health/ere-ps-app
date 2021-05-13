package health.ere.ps.model.idp.client.data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolCurves;
import health.ere.ps.model.idp.crypto.PkiIdentity;

public class IdpJwksDocument {

    private List<IdpKeyDescriptor> keys;

    {
        BrainpoolCurves.init();
    }

    public IdpJwksDocument(List<IdpKeyDescriptor> keys) {
        this.setKeys(keys);
    }

    public IdpJwksDocument() {
    }

    public static IdpJwksDocument constructFromX509Certificate(final PkiIdentity... identities) {
        return IdpJwksDocument.builder()
            .keys(Stream.of(identities)
                .map(identity -> IdpKeyDescriptor.constructFromX509Certificate(identity.getCertificate(),
                    identity.getKeyId(), true))
                .collect(Collectors.toList()))
            .build();
    }

    public List<IdpKeyDescriptor> getKeys() {
        return keys;
    }

    public void setKeys(List<IdpKeyDescriptor> keys) {
        this.keys = keys;
    }

    public static IdpJwksDocumentBuilder builder() {
        return new IdpJwksDocumentBuilder();
    }

    public static class IdpJwksDocumentBuilder {
        private IdpJwksDocument idpJwksDocument;

        public IdpJwksDocumentBuilder() {
            idpJwksDocument = new IdpJwksDocument();
        }

        public IdpJwksDocumentBuilder keys(List<IdpKeyDescriptor> keys) {
            idpJwksDocument.setKeys(keys);

            return this;
        }

        public IdpJwksDocument build() {
            return idpJwksDocument;
        }
    }
}
