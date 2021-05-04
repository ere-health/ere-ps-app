package health.ere.ps.model.idp.client.data;

import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.field.IdpScope;

import java.util.List;
import java.util.Map;

public class UserConsentConfiguration {

    private Map<IdpScope, List<ClaimName>> claimsToBeIncluded;
    private UserConsentDescriptionTexts descriptionTexts;

    public UserConsentConfiguration(Map<IdpScope, List<ClaimName>> claimsToBeIncluded,
                                    UserConsentDescriptionTexts descriptionTexts) {
        this.claimsToBeIncluded = claimsToBeIncluded;
        this.descriptionTexts = descriptionTexts;
    }

    public UserConsentConfiguration() {
    }

    public Map<IdpScope, List<ClaimName>> getClaimsToBeIncluded() {
        return claimsToBeIncluded;
    }

    public void setClaimsToBeIncluded(Map<IdpScope, List<ClaimName>> claimsToBeIncluded) {
        this.claimsToBeIncluded = claimsToBeIncluded;
    }

    public UserConsentDescriptionTexts getDescriptionTexts() {
        return descriptionTexts;
    }

    public void setDescriptionTexts(UserConsentDescriptionTexts descriptionTexts) {
        this.descriptionTexts = descriptionTexts;
    }

    public static UserConsentConfigurationBuilder builder() {
        return new UserConsentConfigurationBuilder();
    }

    public static class UserConsentConfigurationBuilder {
        private UserConsentConfiguration userConsentConfiguration;

        public UserConsentConfigurationBuilder() {
            this.userConsentConfiguration = new UserConsentConfiguration();
        }

        public UserConsentConfigurationBuilder claimsToBeIncluded(
                Map<IdpScope, List<ClaimName>> claimsToBeIncluded) {
            userConsentConfiguration.setClaimsToBeIncluded(claimsToBeIncluded);

            return this;
        }

        public UserConsentConfigurationBuilder descriptionTexts(
                UserConsentDescriptionTexts descriptionTexts) {
            userConsentConfiguration.setDescriptionTexts(descriptionTexts);
            return this;
        }

        public UserConsentConfiguration build() {
            return userConsentConfiguration;
        }
    }
}
