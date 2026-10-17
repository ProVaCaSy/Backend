/**
 * Fachlogik (Business-Schicht).
 *
 * <p><b>Aufgabe:</b> Setzt die Regeln des Urlaubssystems um. Hier ist der Kern der Anwendung.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>{@code AbsenceService}: Anträge anlegen, ändern, stornieren, genehmigen oder ablehnen</li>
 *   <li>{@code VacationBalanceService}: Urlaubskontingent (30 Tage pro Jahr) berechnen</li>
 *   <li>{@code WorkingDayCalculator}: Arbeitstage zählen (ohne Wochenenden und Feiertage)</li>
 *   <li>Prüfung auf Doppelbuchungen im selben Zeitraum</li>
 *   <li>{@code IcsService} und {@code NotificationService}: ICS-Dateien erzeugen und per E-Mail
 *       versenden</li>
 * </ul>
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Arbeitet intern mit Entitäten und gibt nach außen DTOs zurück (Umwandlung über
 *       {@code mapper}). Entitäten verlassen diese Schicht nicht.</li>
 *   <li>Transaktionen ({@code @Transactional}) werden hier gesetzt, nicht im Controller.</li>
 *   <li>Fachliche Fehler werden als Exceptions aus {@code exception} geworfen.</li>
 * </ul>
 *
 * <p><b>Darf verwenden:</b> {@code repository}, {@code model}, {@code mapper}, {@code dto},
 * {@code exception}.
 */
package de.hs_esslingen.provacasy_backend.service;
