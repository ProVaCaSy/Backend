/**
 * Authentifizierung und Autorisierung über Keycloak.
 *
 * <p><b>Aufgabe:</b> Prüft die Tokens von Keycloak (OAuth2 Resource Server) und übersetzt die
 * Rollen aus dem Token in Spring-Rollen. Damit gilt die rollenbasierte Zugriffskontrolle für
 * Professor, Sekretariat und Dekanat.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>{@code SecurityConfig}: Filterkette, geschützte Pfade, {@code @EnableMethodSecurity}</li>
 *   <li>{@code KeycloakJwtAuthenticationConverter}: liest die Rollen aus dem Token</li>
 *   <li>Ein Helfer für den aktuell angemeldeten Benutzer</li>
 * </ul>
 *
 * <p><b>Regeln:</b> Sicherheitsregeln sind hier gebündelt. Die Rechte einzelner Endpunkte stehen
 * als Annotationen im {@code controller}.
 */
package de.hs_esslingen.provacasy_backend.security;
