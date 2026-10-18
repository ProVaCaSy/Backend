/**
 * Datenmodell: JPA-Entitäten und Aufzählungen.
 *
 * <p><b>Aufgabe:</b> Bildet die Tabellen der Datenbank als Java-Klassen ab.
 *
 * <p><b>Gehört hierher (Beispiele):</b>
 * <ul>
 *   <li>Entitäten: {@code User}, {@code Absence}, {@code YearlyEntitlement}</li>
 *   <li>Aufzählungen: {@code AbsenceType} (Urlaub, Konferenz, Sonderurlaub),
 *       {@code AbsenceStatus} (z. B. beantragt, genehmigt, abgelehnt, storniert),
 *       {@code Role} (Professor, Sekretariat, Dekanat)</li>
 * </ul>
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Entitäten werden nie direkt über die API ausgeliefert. Dafür gibt es {@code dto}.</li>
 *   <li>Änderungen am Datenmodell werden zusätzlich als Flyway-Migration festgehalten.</li>
 * </ul>
 *
 * <p><b>Darf verwenden:</b> nur sich selbst und JPA-Annotationen.
 */
package de.hs_esslingen.provacasy_backend.model;
