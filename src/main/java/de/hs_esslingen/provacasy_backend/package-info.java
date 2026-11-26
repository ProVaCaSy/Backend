/**
 * ProVaCaSy (Professoren Vacation Calendar System), Backend.
 *
 * <p>Der Quellcode ist nach Fachlichkeit in Pakete gegliedert (package by feature). Alles, was zu
 * einem Thema gehört, liegt zusammen in einem Paket:
 *
 * <ul>
 *   <li>{@code absence}: Abwesenheiten (anlegen, ändern, löschen, genehmigen, ablehnen)</li>
 *   <li>{@code entitlement}: Urlaubsanspruch pro Person und Jahr</li>
 *   <li>{@code holiday}: Feiertage in Baden-Württemberg</li>
 *   <li>{@code user}: Benutzer aus Keycloak</li>
 * </ul>
 *
 * <p>Innerhalb jedes Fachpakets gilt weiter die Schichtentrennung. Die Schicht steht im
 * Klassennamen, Abhängigkeiten zeigen nur in eine Richtung:
 *
 * <pre>
 * ...Controller  -&gt;  ...Service  -&gt;  ...Repository  -&gt;  Entität
 *       |                 |
 *       v                 v
 *      dto  &lt;---------  ...Mapper
 * </pre>
 *
 * <p>Regeln für alle Fachpakete:
 * <ul>
 *   <li>Controller: implementieren die aus der OpenAPI-Spezifikation generierten Interfaces
 *       ({@code api}), prüfen Rollen per {@code @PreAuthorize}, rufen genau einen Service auf und
 *       geben nur DTOs zurück. Keine Fachlogik, kein Zugriff auf Repositories.</li>
 *   <li>Services: enthalten die Fachlogik und setzen {@code @Transactional}. Entitäten verlassen
 *       den Service nicht. Fachliche Fehler werden als Exceptions aus {@code exception}
 *       geworfen.</li>
 *   <li>Repositories: nur Abfragen (Spring Data JPA), werden nur von Services aufgerufen.</li>
 *   <li>Mapper: MapStruct, nur Abbildung von Feldern zwischen Entität und DTO.</li>
 *   <li>Entitäten: bilden die Tabellen aus der Flyway-Migration ab. Das Schema legt nur Flyway
 *       an, Hibernate prüft es nur ({@code ddl-auto=validate}).</li>
 * </ul>
 *
 * <p>Querschnittsthemen, die alle Fachpakete betreffen, liegen in eigenen Paketen:
 * {@code security} (Keycloak, Rollen), {@code config} (technische Konfiguration) und
 * {@code exception} (zentrale Fehlerbehandlung). Die Pakete {@code api} und {@code dto} werden
 * beim Maven-Build aus der OpenAPI-Spezifikation generiert.
 *
 * <p>Eine ausführliche Beschreibung mit Beispielen steht in {@code docs/architecture.md}.
 */
package de.hs_esslingen.provacasy_backend;
