
package de.hs_esslingen.provacasy_backend.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            KeycloakJwtAuthenticationConverter roleConverter,
            @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver exceptionResolver
    ) throws Exception {

        http
            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    HttpMethod.GET,
                    "/actuator/health",
                    "/actuator/health/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/users/me"
                ).authenticated()


                .anyRequest().hasAnyRole(
                    "PROFESSOR",
                    "SECRETARIAT",
                    "DEAN"
                )
            )

            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler((request, response, exception) -> {
                    if (exceptionResolver.resolveException(
                            request, response, null, exception) == null) {
                        response.sendError(403);
                    }
                })
            )

            .oauth2ResourceServer(oauth ->
                oauth.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(roleConverter)
                )
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
            List.of("http://localhost:5173")
        );

        config.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE")
        );

        config.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", config);

        return source;
    }
}
