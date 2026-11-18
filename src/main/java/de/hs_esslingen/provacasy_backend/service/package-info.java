/**
 * Fachlogik (Business-Schicht).
 *
 * <p><b>Aufgabe:</b> Setzt die Regeln des Urlaubssystems um. Hier ist der Kern der Anwendung.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>{@code AbsenceService}: Abwesenheiten anlegen, ändern, löschen, genehmigen oder
 *       ablehnen. Prüft Überschneidungen, setzt den Status (Professor: offen, Sekretariat und
 *       Dekanat für andere: genehmigt) und erhöht bei jeder Änderung die Version.</li>
 *   <li>{@code UserService}: legt Benutzer beim ersten Login an und aktualisiert Name und
 *       E-Mail aus dem Token</li>
 *   <li>{@code StatisticsService}: berechnet Anspruch (Standard 30 Tage), genommene Tage und
 *       Resturlaub und markiert Überplanung</li>
 *   <li>{@code ExportService}: Export der Statistik als CSV oder PDF</li>
 *   <li>{@code PublicHolidayService}: Feiertage in Baden-Württemberg aus der Tabelle, bei
 *       fehlendem Jahr von der Feiertags-API, zusätzlich im Cache; ergänzt 24.12. und 31.12.</li>
 *   <li>{@code WorkingDayCalculator}: zählt Urlaubstage ohne Wochenenden und Feiertage</li>
 *   <li>{@code MailService} und {@code IcsService}: ICS-Datei erzeugen (UID aus der ID der
 *       Abwesenheit, Version als SEQUENCE) und per Mail mit Deeplink versenden</li>
 * </ul>
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Arbeitet intern mit Entitäten und gibt nach außen DTOs zurück (Umwandlung über
 *       {@code mapper}). Entitäten verlassen diese Schicht nicht.</li>
 *   <li>Transaktionen ({@code @Transactional}) werden hier gesetzt, nicht im Controller.</li>
 *   <li>Fachliche Fehler werden als Exceptions aus {@code exception} geworfen.</li>
 *   <li>Eine Überplanung (mehr Tage als der Anspruch) wird nicht blockiert, sondern nur in der
 *       Statistik markiert.</li>
 *   <li>Eigentümerprüfung ("Professoren nur eigene Einträge, nur solange offen") liegt hier.</li>
 * </ul>
 *
 * <p><b>Darf verwenden:</b> {@code repository}, {@code model}, {@code mapper}, {@code dto},
 * {@code exception}.
 */
package de.hs_esslingen.provacasy_backend.service;
