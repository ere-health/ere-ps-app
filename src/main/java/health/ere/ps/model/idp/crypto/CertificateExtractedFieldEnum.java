
package health.ere.ps.model.idp.crypto;

public enum CertificateExtractedFieldEnum {
    PROFESSION_OID("professionOID"),
    GIVEN_NAME("given_name"),
    FAMILY_NAME("family_name"),
    ORGANIZATION_NAME("organizationName"),
    ID_NUMMER("idNummer");

    private final String fieldname;

    CertificateExtractedFieldEnum(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFieldname() {
        return fieldname;
    }
}
