/**
 * Technische Konfiguration der Anwendung.
 *
 * <p><b>Aufgabe:</b> Legt fest, wie die Anwendung technisch zusammengesetzt ist, nicht was sie
 * fachlich tut.
 *
 * <p><b>Gehört hierher:</b> {@code @Configuration}-Klassen für
 * <ul>
 *   <li>{@code CorsConfig}: erlaubt nur die Origin des Frontends, die Methoden GET, POST, PUT
 *       und DELETE sowie die Header {@code Authorization} und {@code Content-Type}</li>
 *   <li>{@code CacheConfig}: Cache für die Feiertage je Jahr</li>
 *   <li>{@code MailConfig}: Mailversand (SMTP-Server ist über Einstellungen austauschbar)</li>
 *   <li>der REST-Client für die Feiertags-API</li>
 * </ul>
 *
 * <p><b>Regeln:</b> Keine Fachlogik. Die Sicherheitskonfiguration liegt im Paket
 * {@code security}. Die API wird in der OpenAPI-Spezifikation beschrieben und nicht über
 * Annotationen, deshalb gibt es hier keine OpenAPI-Konfiguration.
 */
package de.hs_esslingen.provacasy_backend.config;
