package me.ronygomes.ums.api.config;

import me.ronygomes.ums.api.helper.RoleHelper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class KeyCloakJwtConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private static final String KEYCLOAK_CLIENT_ID = "ums-client-webapp";

    private final Converter<Jwt, List<SimpleGrantedAuthority>> AUTHORITIES_CONVERTER = jwt -> {

        var resourceAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("resource_access", Collections.emptyMap());
        var clientAccess = (Map<String, Object>) resourceAccess.getOrDefault(KEYCLOAK_CLIENT_ID, Collections.emptyMap());
        var clientRoles = (List<String>) clientAccess.getOrDefault("roles", Collections.emptyList());

        return clientRoles.stream()
                .map(RoleHelper::validateAndConvert)
                .toList();
    };

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, AUTHORITIES_CONVERTER.convert(jwt),
                jwt.getClaimAsString(StandardClaimNames.EMAIL));
    }
}
