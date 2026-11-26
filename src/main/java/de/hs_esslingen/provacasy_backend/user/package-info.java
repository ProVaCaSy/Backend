/**
 * Benutzer aus Keycloak.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>{@code User} (Tabelle {@code app_user}), {@code UserRepository}, {@code UserMapper}</li>
 *   <li>{@code UserService}: legt Benutzer beim ersten Login an und aktualisiert Name und E-Mail
 *       aus dem Token</li>
 *   <li>{@code UserController}: {@code GET /users/me} und {@code GET /users?search=}</li>
 * </ul>
 *
 * <p><b>Regeln:</b> Die Id ist die User-ID aus Keycloak und wird nicht von der Datenbank
 * erzeugt. Die Rolle wird nicht gespeichert, sie kommt immer aus dem Token.
 */
package de.hs_esslingen.provacasy_backend.user;
