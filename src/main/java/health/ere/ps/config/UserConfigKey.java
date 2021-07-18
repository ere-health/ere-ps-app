package health.ere.ps.config;

public enum UserConfigKey {
    ERIXA_HOTFOLDER("erixa.hotfolder"),
    ERIXA_DRUGSTORE_EMAIL_ADDRESS("erixa.drugstore.email.address"),
    ERIXA_USER_EMAIL("erixa.user.email"),
    ERIXA_USER_PASSWORD("erixa.user.password"),
    CONNECTOR_BASE_URL("connector.base.url"),
    CONNECTOR_MANDANT_ID("connector.mandant-id"),
    CONNECTOR_WORKPLACE_ID("connector.workplace-id"),
    CONNECTOR_CLIENT_SYSTEM_ID("connector.client-system-id"),
    CONNECTOR_USER_ID("connector.user.id"),
    CONNECTOR_TV_MODE("connector.tvMode"),
    EXTRACTOR_TEMPLATE_PROFILE("extractor.template.profile");

    public final String key;

    UserConfigKey(String key) {
        this.key = key;
    }
}
