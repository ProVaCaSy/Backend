/**
 * Datenzugriff (Persistenz-Schicht).
 *
 * <p><b>Aufgabe:</b> Liest und schreibt Entitäten in der Datenbank. Die Spring-Data-Repositories
 * übernehmen hier die Rolle der klassischen DAOs.
 *
 * <p><b>Gehört hierher:</b> Interfaces wie {@code AbsenceRepository} (z. B. eine Abfrage nach
 * überlappenden Abwesenheiten), {@code UserRepository}, {@code YearlyEntitlementRepository}.
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Nur Interfaces und Abfragen ({@code JpaRepository}, abgeleitete Methoden oder
 *       {@code @Query}), keine Fachlogik.</li>
 *   <li>Wird ausschließlich vom {@code service} aufgerufen.</li>
 * </ul>
 *
 * <p><b>Darf verwenden:</b> {@code model}.
 */
package de.hs_esslingen.provacasy_backend.repository;
