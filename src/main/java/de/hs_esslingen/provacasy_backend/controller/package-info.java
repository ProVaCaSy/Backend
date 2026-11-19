/**
 * REST-Schnittstelle (Web-Schicht).
 *
 * <p><b>Aufgabe:</b> Nimmt HTTP-Anfragen entgegen, validiert die Eingaben ({@code @Valid}), ruft
 * genau einen Service auf und gibt DTOs mit dem passenden HTTP-Status zurück. Die Controller
 * implementieren die Interfaces, die beim Maven-Build aus der OpenAPI-Spezifikation
 * ({@code src/main/resources/openapi/provacasy-api.yaml}) generiert werden. Die Zugriffsrechte
 * pro Endpunkt werden hier festgelegt (z. B. {@code @PreAuthorize}).
 *
 * <p><b>Gehört hierher:</b> {@code AbsenceController}, {@code UserController},
 * {@code StatisticsController}, {@code PublicHolidayController} sowie der Endpunkt zum Ändern des
 * Urlaubsanspruchs ({@code PUT /vacation-entitlements/{userId}}).
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Keine Fachlogik. Berechnungen und Prüfungen gehören in den {@code service}.</li>
 *   <li>Kein Zugriff auf {@code repository} und keine Entitäten aus {@code model}.</li>
 *   <li>Ein- und Ausgabe ausschließlich über {@code dto}.</li>
 *   <li>Die Rolle wird per {@code @PreAuthorize} geprüft. Einschränkungen wie "Professoren nur
 *       eigene Einträge" prüft der {@code service}.</li>
 * </ul>
 *
 * <p><b>Darf verwenden:</b> {@code service}, {@code dto}.
 */
package de.hs_esslingen.provacasy_backend.controller;
