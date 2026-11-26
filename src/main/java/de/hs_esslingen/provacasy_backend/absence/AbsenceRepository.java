package de.hs_esslingen.provacasy_backend.absence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.hs_esslingen.provacasy_backend.user.User;

/**
 * Datenzugriff auf die Tabelle absence. Enthält nur Abfragen, die Regeln stehen im AbsenceService.
 */
public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    /**
     * Alle Abwesenheiten, die den Zeitraum from bis to berühren (für den Kalender, GET /api/absences).
     * Eine Abwesenheit berührt den Zeitraum, wenn sie vor dessen Ende beginnt und nach dessen Anfang endet.
     * Der betroffene User wird gleich mitgeladen, weil der Kalender bei jedem Eintrag den Namen zeigt.
     */
    @Query("""
            select a from Absence a
            join fetch a.user
            where a.startDate <= :to and a.endDate >= :from
            order by a.startDate
            """)
    List<Absence> findInPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Wie findInPeriod, aber nur mit einem bestimmten Status (Parameter status in GET /api/absences). */
    @Query("""
            select a from Absence a
            join fetch a.user
            where a.startDate <= :to and a.endDate >= :from and a.status = :status
            order by a.startDate
            """)
    List<Absence> findInPeriodWithStatus(@Param("from") LocalDate from, @Param("to") LocalDate to,
                                         @Param("status") AbsenceStatus status);

    /** Alle Abwesenheiten mit einem Status, zum Beispiel die offenen Anträge für das Dekanat. */
    List<Absence> findByStatusOrderByStartDateAsc(AbsenceStatus status);

    /**
     * Gibt es für diese Person schon eine Abwesenheit, die sich mit start bis end überschneidet?
     * Abgelehnte Einträge zählen nicht. Für das Anlegen (POST /api/absences).
     * Die Datenbank sichert das zusätzlich mit dem Constraint absence_no_overlap ab.
     */
    @Query("""
            select count(a) > 0 from Absence a
            where a.user = :user
              and a.status <> de.hs_esslingen.provacasy_backend.absence.AbsenceStatus.REJECTED
              and a.startDate <= :end and a.endDate >= :start
            """)
    boolean existsOverlap(@Param("user") User user, @Param("start") LocalDate start,
                          @Param("end") LocalDate end);

    /**
     * Wie existsOverlap, aber ohne den Eintrag mit der id excludedId.
     * Für das Ändern (PUT /api/absences/{id}), sonst würde sich der Eintrag mit sich selbst überschneiden.
     */
    @Query("""
            select count(a) > 0 from Absence a
            where a.user = :user
              and a.id <> :excludedId
              and a.status <> de.hs_esslingen.provacasy_backend.absence.AbsenceStatus.REJECTED
              and a.startDate <= :end and a.endDate >= :start
            """)
    boolean existsOverlapExcluding(@Param("user") User user, @Param("start") LocalDate start,
                                   @Param("end") LocalDate end, @Param("excludedId") Long excludedId);
}
