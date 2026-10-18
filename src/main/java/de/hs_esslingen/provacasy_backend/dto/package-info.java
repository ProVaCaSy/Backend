/**
 * Data Transfer Objects: Daten, die über die REST-Schnittstelle ein- und ausgehen.
 *
 * <p><b>Aufgabe:</b> Bilden den Vertrag der API. Sie trennen das, was Clients sehen, vom internen
 * Datenmodell. So können sich Entitäten ändern, ohne die API zu brechen, und es werden keine
 * internen Felder versehentlich preisgegeben.
 *
 * <p><b>Gehört hierher:</b> Java-{@code record}s wie {@code AbsenceCreateRequest},
 * {@code AbsenceResponse}, {@code VacationSummaryResponse}.
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Request-DTOs tragen die Validierungsregeln (Bean Validation, z. B. {@code @NotNull}).</li>
 *   <li>Keine Logik und keine Abhängigkeit zu {@code model}.</li>
 *   <li>Namensschema: {@code ...Request} für Eingaben, {@code ...Response} für Ausgaben.</li>
 * </ul>
 */
package de.hs_esslingen.provacasy_backend.dto;
