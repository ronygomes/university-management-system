package me.ronygomes.ums.api.config;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;

@ExtendWith(SpringExtension.class)
@AutoConfigureWireMock(port = 8000)
public class KeyCloakJwtConverterTest {

    private static final String KEYCLOAK_EXPIRED_JWT_TOKEN = """
            eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtMXZXZ0pMeGRUUDltM1BTZHFEaVljdUZxQW8yZXJ1Nk5DLVpzNjFaUkJrIn0.eyJleHAiOjE3Mjc5NjUyMDIsImlhdCI6MTcyNzk2NDkwMiwianRpIjoiMmVmZWQ3MmQtM2Y5OC00M2YzLTljMmUtODNkMWEwMzc0NzA4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDAwL3JlYWxtcy91bXMiLCJzdWIiOiIwYjQzMjI2MS01OTAyLTRmY2UtYmUzMC1mZjI4YjMwZGUyYjciLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ1bXMtY2xpZW50LXdlYmFwcCIsInNlc3Npb25fc3RhdGUiOiIwYTcxZDk2Ni05NzYyLTQ3NzYtYTEwMC04NjVhY2IwZTU3ODEiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6ODEwMCJdLCJyZXNvdXJjZV9hY2Nlc3MiOnsidW1zLWNsaWVudC13ZWJhcHAiOnsicm9sZXMiOlsiYWRtaW4iXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJzaWQiOiIwYTcxZDk2Ni05NzYyLTQ3NzYtYTEwMC04NjVhY2IwZTU3ODEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWRtaW4iLCJnaXZlbl9uYW1lIjoiIiwiZmFtaWx5X25hbWUiOiIiLCJlbWFpbCI6ImFkbWluQHVtcy5kZXYifQ.Atn6VW-qMBJRTu4Yyl3oYjC4_kA0AKKtTJ0aG3gkUayRwbfbQlOa-1ezv90GdyP07zsPU5g2bUQj8ttneBh3qfaOvsEfxYijwyM-2q42JFVblnHCA56Wgu0ygKNEU7yzZToMmpB4nYrV3cOjAFu2I7y4lgmjN_xBfGhM6zRfffiZn_jDF-vtmRQdnWemoyV2Kp-gxZXhlsBXiz_HHEQwGFTOU63oTO0atIosSYtTjFXPKQP8QzgbyuYQjhz1l0p22wFcJRWglBtfn5n7ekdIEDU5FK_lBLbEuCiF871ZdIsDObfuy9RiBMu_hOmlRGJxDoS52z40O4PI4MqCZItySA
            """;

    private static final String ISSUER_URL = "http://localhost:8000/realms/ums";

    private KeyCloakJwtConverter converter;

    @BeforeEach
    void setup() {

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/realms/ums/.well-known/openid-configuration"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBodyFile("openid-configuration.json")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/realms/ums/protocol/openid-connect/certs"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBodyFile("certs.json")));

        converter = new KeyCloakJwtConverter();
    }

    @Test
    void testRole() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(ISSUER_URL).build();

        // Doesn't parse expired token by default so setting validator to always pass
        // Alternative is to create JWT token using builder, but want to test Keycloak generated token directly
        OAuth2TokenValidator<Jwt> jwtValidator = token -> OAuth2TokenValidatorResult.success();
        jwtDecoder.setJwtValidator(jwtValidator);

        Jwt jwt = jwtDecoder.decode(KEYCLOAK_EXPIRED_JWT_TOKEN);

        JwtAuthenticationToken token = converter.convert(jwt);
        Assertions.assertEquals(1, token.getAuthorities().size());
        Assertions.assertEquals("ROLE_ADMIN", new ArrayList<>(token.getAuthorities()).get(0).getAuthority());
    }
}
