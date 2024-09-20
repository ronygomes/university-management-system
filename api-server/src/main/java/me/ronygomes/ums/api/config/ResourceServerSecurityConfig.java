package me.ronygomes.ums.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class ResourceServerSecurityConfig {

    @Bean
    SecurityFilterChain configure(HttpSecurity http, KeyCloakJwtConverter converter) throws Exception {

        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(AbstractHttpConfigurer::disable);

        http.exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, authException) -> {
            response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Restricted Content\"");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }));

        http.oauth2ResourceServer(rs -> rs.jwt(config -> config.jwtAuthenticationConverter(converter)));

        return http.build();
    }
}
