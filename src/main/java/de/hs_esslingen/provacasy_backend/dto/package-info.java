/**
 * Data Transfer Objects: Daten, die über die REST-Schnittstelle ein- und ausgehen.
 *
 * <p><b>Aufgabe:</b> Bilden den Vertrag der API. Sie trennen das, was Clients sehen, vom internen
 * Datenmodell. So können sich Entitäten ändern, ohne die API zu brechen, und es werden keine
 * internen Felder versehentlich preisgegeben.
 *
 * <p><b>Gehört hierher:</b> Die Klassen werden beim Maven-Build aus der OpenAPI-Spezifikation
 * ({@code src/main/resources/openapi/provacasy-api.yaml}) generiert, z. B.
 * {@code AbsenceCreateRequest}, {@code AbsenceResponse}, {@code StatisticsResponse}. Sie werden
 * nicht von Hand geschrieben. Änderungen erfolgen in der Spezifikation.
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Request-DTOs tragen die Validierungsregeln (Bean Validation, aus der Spezifikation).</li>
 *   <li>Keine Logik und keine Abhängigkeit zu {@code model}.</li>
 *   <li>Namensschema: {@code ...Request} für Eingaben, {@code ...Response} für Ausgaben.</li>
 *   <li>Dieses Paket enthält von Hand nur diese Beschreibung.</li>
 * </ul>
 */
package de.hs_esslingen.provacasy_backend.dto;
