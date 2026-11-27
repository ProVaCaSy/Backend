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
// Nur ein Interface, keine Klasse: Spring Data erzeugt beim Start selbst eine Klasse, die es
// umsetzt, und stellt sie als Bean bereit. Der Service bekommt sie per Konstruktor.
// JpaRepository<Absence, Long>: Entity Typ und Typ des Primärschlüssels. Damit gibt es
// geschenkt: save(), findById(), findAll(), deleteById(), count() usw.
public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    // @Query enthält JPQL, nicht SQL: Man fragt nach Klassen und Feldern (Absence, a.startDate),
    // nicht nach Tabellen und Spalten (absence, start_date). Hibernate übersetzt das in SQL.
    // :from und :to sind Platzhalter, @Param verbindet sie mit den Methodenparametern.
    // Spring prüft die Abfrage schon beim Start. Ein Tippfehler fällt also sofort auf.
    //
    // Überschneidung zweier Zeiträume: A überschneidet B, wenn A beginnt, bevor B endet, und
    // A endet, nachdem B begonnen hat. Mit <= und >= zählen auch Zeiträume, die sich nur an
    // einem Tag berühren (wie daterange '[]' in der Datenbank).
    //
    // join fetch: Lädt die User in derselben Abfrage mit. Ohne das gäbe es wegen LAZY für jeden
    // Eintrag eine eigene Abfrage, sobald der Kalender den Namen braucht (das "N+1 Problem").
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

    // Abgeleitete Abfrage (derived query), ganz ohne @Query: Spring liest den Methodennamen.
    // findBy...      = select ... where
    // Status         = a.status = ?
    // OrderBy...Asc  = order by a.startDate aufsteigend
    /** Alle Abwesenheiten mit einem Status, zum Beispiel die offenen Anträge für das Dekanat. */
    List<Absence> findByStatusOrderByStartDateAsc(AbsenceStatus status);

    // Warum default Methode plus Abfrage mit Parameter statt einer festen Konstante?
    // Eine Enum Konstante direkt in JPQL (a.status <> ...AbsenceStatus.REJECTED) schreibt
    // Hibernate als 'REJECTED'::AbsenceStatus ins SQL, also mit dem Java Klassennamen als Typ.
    // Unser PostgreSQL Typ heißt aber absence_status, das ergab "type absencestatus does not
    // exist" (gefunden durch AbsenceRepositoryTest). Als Parameter übergeben klappt es, deshalb
    // geben die default Methoden REJECTED als Parameter an die Abfrage weiter. Für den Service
    // bleiben die Methoden einfach: existsOverlap(user, start, end).
    /**
     * Gibt es für diese Person schon eine Abwesenheit, die sich mit start bis end überschneidet?
     * Abgelehnte Einträge zählen nicht. Für das Anlegen (POST /api/absences).
     * Die Datenbank sichert das zusätzlich mit dem Constraint absence_no_overlap ab.
     */
    default boolean existsOverlap(User user, LocalDate start, LocalDate end) {
        return countOverlaps(user, start, end, AbsenceStatus.REJECTED) > 0;
    }

    /**
     * Wie existsOverlap, aber ohne den Eintrag mit der id excludedId.
     * Für das Ändern (PUT /api/absences/{id}), sonst würde sich der Eintrag mit sich selbst überschneiden.
     */
    default boolean existsOverlapExcluding(User user, LocalDate start, LocalDate end, Long excludedId) {
        return countOverlapsExcluding(user, start, end, excludedId, AbsenceStatus.REJECTED) > 0;
    }

    // count(a) zählt nur, wir brauchen die Einträge selbst nicht, das ist schneller.
    // a.user = :user vergleicht über den Primärschlüssel, also die user_id.
    /** Anzahl der Abwesenheiten dieser Person zwischen start und end, ohne den Status ignored. */
    @Query("""
            select count(a) from Absence a
            where a.user = :user
              and a.status <> :ignored
              and a.startDate <= :end and a.endDate >= :start
            """)
    long countOverlaps(@Param("user") User user, @Param("start") LocalDate start,
                       @Param("end") LocalDate end, @Param("ignored") AbsenceStatus ignored);

    /** Wie countOverlaps, aber ohne den Eintrag mit der id excludedId. */
    @Query("""
            select count(a) from Absence a
            where a.user = :user
              and a.id <> :excludedId
              and a.status <> :ignored
              and a.startDate <= :end and a.endDate >= :start
            """)
    long countOverlapsExcluding(@Param("user") User user, @Param("start") LocalDate start,
                                @Param("end") LocalDate end, @Param("excludedId") Long excludedId,
                                @Param("ignored") AbsenceStatus ignored);
}
