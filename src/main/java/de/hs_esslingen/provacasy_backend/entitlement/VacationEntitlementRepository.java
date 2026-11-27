package de.hs_esslingen.provacasy_backend.entitlement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Datenzugriff auf die Tabelle vacation_entitlement.
 * Den Anspruch einer Person in einem Jahr liefert schon findById(new VacationEntitlementId(userId, year)).
 */
// Zweiter Typparameter ist hier die Schlüsselklasse VacationEntitlementId, weil der
// Primärschlüssel aus zwei Spalten besteht.
public interface VacationEntitlementRepository extends JpaRepository<VacationEntitlement, VacationEntitlementId> {

    // Abgeleitete Abfrage: "IdYear" heißt "Feld year innerhalb von id", also where v.id.year = ?
    /** Alle Ansprüche eines Jahres, für die Gesamtstatistik von Sekretariat und Dekanat. */
    List<VacationEntitlement> findByIdYear(Short year);

    // Als abgeleitete Abfrage hieße das findFirstByIdUserIdAndIdYearLessThanOrderByIdYearDesc.
    // Mit @Query bleibt der Name kurz und die Abfrage lesbar.
    // "order by ... desc limit 1": nach Jahr absteigend sortieren und nur den ersten nehmen,
    // also das jüngste Jahr vor :year.
    // Optional: Es kann sein, dass es noch gar keinen Eintrag gibt (dann gelten 30 Tage).
    // Optional zwingt den Aufrufer, diesen Fall zu behandeln, statt mit null zu arbeiten.
    /**
     * Der jüngste Anspruch einer Person vor dem angegebenen Jahr.
     * Für ein neues Jahr wird der Wert des Vorjahres übernommen (US-S5), ohne Eintrag gelten 30 Tage.
     */
    @Query("""
            select v from VacationEntitlement v
            where v.id.userId = :userId and v.id.year < :year
            order by v.id.year desc
            limit 1
            """)
    Optional<VacationEntitlement> findLatestBefore(@Param("userId") UUID userId, @Param("year") Short year);
}
