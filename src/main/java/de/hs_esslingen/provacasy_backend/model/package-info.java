/**
 * Datenmodell: JPA-Entitäten und Aufzählungen.
 *
 * <p><b>Aufgabe:</b> Bildet die Tabellen der Datenbank (Flyway-Migration {@code V1__init.sql})
 * als Java-Klassen ab.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>Entitäten: {@code User} (Tabelle {@code app_user}), {@code Absence},
 *       {@code VacationEntitlement}, {@code PublicHoliday}</li>
 *   <li>Aufzählung: {@code AbsenceStatus} mit {@code OPEN}, {@code APPROVED}, {@code REJECTED}.
 *       Einen Status "storniert" gibt es nicht, stornierte Einträge werden gelöscht.</li>
 * </ul>
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Entitäten werden nie direkt über die API ausgeliefert. Dafür gibt es {@code dto}.</li>
 *   <li>Es gibt keine Unterscheidung der Abwesenheit nach Art, jede Abwesenheit zählt auf den
 *       Urlaubsanspruch.</li>
 *   <li>Die Id von {@code User} ist die User-ID aus Keycloak und wird nicht von der Datenbank
 *       erzeugt.</li>
 *   <li>{@code Absence.status} ist ein PostgreSQL-Enum ({@code absence_status}), {@code version}
 *       ist die Versionsnummer ({@code @Version}) und zugleich die SEQUENCE der ICS-Datei.</li>
 *   <li>Änderungen am Datenmodell werden zusätzlich als Flyway-Migration festgehalten.</li>
 * </ul>
 *
 * <p><b>Darf verwenden:</b> nur sich selbst und JPA-Annotationen.
 */
package de.hs_esslingen.provacasy_backend.model;
