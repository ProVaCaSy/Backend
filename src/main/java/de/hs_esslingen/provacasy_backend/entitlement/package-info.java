/**
 * Urlaubsanspruch pro Benutzer und Jahr.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>{@code VacationEntitlement} (Tabelle {@code vacation_entitlement}) mit dem
 *       zusammengesetzten Schlüssel {@code VacationEntitlementId} aus Benutzer und Jahr</li>
 *   <li>{@code VacationEntitlementRepository}, {@code VacationEntitlementMapper}</li>
 *   <li>Service und Controller für {@code PUT /vacation-entitlements/{userId}}</li>
 * </ul>
 *
 * <p><b>Regeln:</b> Standard sind 30 Tage. Für ein neues Jahr wird der Wert des Vorjahres
 * übernommen. Vergangene Jahre bleiben unverändert.
 */
package de.hs_esslingen.provacasy_backend.entitlement;
