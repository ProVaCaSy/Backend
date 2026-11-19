/**
 * Authentifizierung und Autorisierung über Keycloak.
 *
 * <p><b>Aufgabe:</b> Prüft die Tokens von Keycloak (OAuth2 Resource Server: Signatur, Aussteller,
 * Ablaufzeit über den öffentlichen Schlüssel, JWKS) und übersetzt die Gruppen aus dem Claim
 * {@code groups} in Spring-Rollen. Damit gilt die rollenbasierte Zugriffskontrolle für
 * Professor, Sekretariat und Dekanat.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>{@code SecurityConfig}: Filterkette, geschützte Pfade, {@code @EnableMethodSecurity}</li>
 *   <li>{@code KeycloakJwtAuthenticationConverter}: bildet die Gruppen {@code professors},
 *       {@code staff} und {@code dekanat} auf die Rollen ab. Bei mehreren Gruppen gilt die
 *       höchste Rolle. Studierende und Benutzer ohne passende Gruppe erhalten keine Rolle.</li>
 *   <li>Ein Helfer für den aktuell angemeldeten Benutzer</li>
 * </ul>
 *
 * <p><b>Regeln:</b> Sicherheitsregeln sind hier gebündelt. Die Rechte einzelner Endpunkte stehen
 * als Annotationen im {@code controller}. Das Backend stellt keine eigenen Tokens aus und
 * verwaltet keine Passwörter. Maßgeblich ist immer das Token.
 */
package de.hs_esslingen.provacasy_backend.security;
