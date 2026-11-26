/**
 * Gesetzliche Feiertage in Baden-Württemberg.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>{@code PublicHoliday} (Tabelle {@code public_holiday}), {@code PublicHolidayRepository},
 *       {@code PublicHolidayMapper}</li>
 *   <li>{@code PublicHolidayService}: liest die Feiertage aus der Tabelle, holt fehlende Jahre
 *       von der Feiertags-API, hält sie im Cache und ergänzt den 24.12. und 31.12.</li>
 *   <li>{@code WorkingDayCalculator}: zählt Urlaubstage ohne Wochenenden und Feiertage</li>
 *   <li>{@code PublicHolidayController}</li>
 * </ul>
 *
 * <p><b>Regeln:</b> Die Feiertags-API ist der einzige Internetzugriff des Backends.
 */
package de.hs_esslingen.provacasy_backend.holiday;
