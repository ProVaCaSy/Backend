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
public interface VacationEntitlementRepository extends JpaRepository<VacationEntitlement, VacationEntitlementId> {

    /** Alle Ansprüche eines Jahres, für die Gesamtstatistik von Sekretariat und Dekanat. */
    List<VacationEntitlement> findByIdYear(Short year);

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
