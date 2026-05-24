package me.ronygomes.ums.api.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

    @Value("${ums.keycloak.server-url}")
    private String serverUrl;

    @Value("${ums.keycloak.master-realm}")
    private String masterRealm;

    @Value("${ums.keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${ums.keycloak.admin-client-secret}")
    private String adminClientSecret;

    @Bean
    public Keycloak keycloakAdminClient() {
        // Placeholder — credentials configured via application.properties.
        // Replace `<TBD>` placeholders with a real service-account client + secret.
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(masterRealm)
                .clientId(adminClientId)
                .clientSecret(adminClientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
