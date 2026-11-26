/**
 * Abwesenheiten der Benutzer.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>{@code Absence} (Tabelle {@code absence}) und {@code AbsenceStatus} mit {@code OPEN},
 *       {@code APPROVED}, {@code REJECTED}. Einen Status "storniert" gibt es nicht.</li>
 *   <li>{@code AbsenceRepository}, {@code AbsenceMapper}</li>
 *   <li>{@code AbsenceService}: anlegen, ändern, löschen, genehmigen, ablehnen. Prüft
 *       Überschneidungen, setzt den Status (Professor: offen, Sekretariat und Dekanat für andere:
 *       genehmigt) und prüft "Professoren nur eigene Einträge, nur solange offen".</li>
 *   <li>{@code AbsenceController}</li>
 * </ul>
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Es gibt keine Art der Abwesenheit, jede Abwesenheit zählt auf den Urlaubsanspruch.</li>
 *   <li>{@code status} ist ein PostgreSQL-Enum ({@code absence_status}). {@code version} ist die
 *       Versionsnummer ({@code @Version}) und zugleich die SEQUENCE der ICS-Datei.</li>
 *   <li>Die Überschneidungsabfrage im Repository ist eine Vorprüfung. Letzte Sicherung ist der
 *       Constraint {@code absence_no_overlap} in der Datenbank.</li>
 * </ul>
 */
package de.hs_esslingen.provacasy_backend.absence;
