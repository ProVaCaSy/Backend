package de.hs_esslingen.provacasy_backend.absence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import de.hs_esslingen.provacasy_backend.user.User;

// Integrationstest gegen ein echtes PostgreSQL. H2 geht nicht, weil das Schema btree_gist und
// den eigenen Enum Typ absence_status nutzt.
// @DataJpaTest: startet nur den Datenbank Teil von Spring (Entities, Repositories, Flyway),
// kein Web und keine Security. Jeder Test läuft in einer Transaktion, die am Ende
// zurückgerollt wird. Die Tests stören sich deshalb nicht gegenseitig.
// @AutoConfigureTestDatabase(NONE): Spring soll die Datenbank NICHT durch eine eingebaute
// H2 ersetzen, sondern die aus dem Container nehmen.
// @Testcontainers: JUnit startet die Felder mit @Container vor den Tests und stoppt sie danach.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AbsenceRepositoryTest {

    // Ein echter postgres:17 Container, die gleiche Version wie in Compose und CI.
    // static: ein Container für alle Tests der Klasse, nicht einer pro Test (spart Zeit).
    // @ServiceConnection: Spring nimmt Adresse, Benutzer und Passwort direkt vom Container,
    // man muss keine URL von Hand eintragen. Beim Start spielt Flyway V1__init.sql ein.
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private AbsenceRepository absenceRepository;

    // Hilfsmittel für Tests: speichert Objekte direkt, ohne Repository, und erlaubt
    // flush (jetzt an die Datenbank schicken) und clear (Zwischenspeicher von Hibernate leeren).
    @Autowired
    private TestEntityManager entityManager;

    private User prof;

    // Läuft vor jedem Test. Eine Abwesenheit braucht einen User (Fremdschlüssel user_id).
    @BeforeEach
    void setUp() {
        prof = entityManager.persist(new User(UUID.randomUUID(), "Max", "Mustermann", "max@example.com"));
    }

    @Test
    void savedAbsenceShouldBeLoadedFromDatabase() {
        // vorbereiten und speichern
        Absence saved = absenceRepository.save(new Absence(prof, prof,
                LocalDate.of(2026, 12, 21), LocalDate.of(2026, 12, 23), "Urlaub", AbsenceStatus.OPEN));
        // flush schickt das INSERT wirklich an PostgreSQL. clear leert den Zwischenspeicher,
        // damit findById danach wirklich aus der Datenbank liest und nicht das Objekt
        // aus dem Speicher zurückgibt. Sonst würden wir Hibernate testen, nicht die Datenbank.
        entityManager.flush();
        entityManager.clear();

        // ausführen
        Absence loaded = absenceRepository.findById(saved.getId()).orElseThrow();

        // prüfen
        assertEquals(prof.getId(), loaded.getUser().getId());
        assertEquals(prof.getId(), loaded.getCreatedFrom().getId());
        assertEquals(LocalDate.of(2026, 12, 21), loaded.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 23), loaded.getEndDate());
        assertEquals("Urlaub", loaded.getTitle());
        // Der Status ging als PostgreSQL Enum hin und kam als Java Enum zurück (NAMED_ENUM).
        assertEquals(AbsenceStatus.OPEN, loaded.getStatus());
        // @Version startet bei 0, @CreationTimestamp hat die Zeit gesetzt.
        assertEquals((short) 0, loaded.getVersion());
        assertNotNull(loaded.getCreatedAt());
        // Noch nie geändert: beide Felder leer (Constraint absence_updated_check).
        assertNull(loaded.getUpdatedFrom());
        assertNull(loaded.getUpdatedAt());
    }

    // ---------------------------------------------------------------------------------------
    // Überschneidung (Doppelbuchung). Bestehender Eintrag in allen Tests: 21.12. bis 23.12.
    // ---------------------------------------------------------------------------------------

    @Test
    void overlappingPeriodShouldBeDetected() {
        saveAbsence(prof, 21, 23, AbsenceStatus.APPROVED);

        // 22.12. bis 24.12. liegt teilweise im bestehenden Zeitraum
        assertTrue(absenceRepository.existsOverlap(prof, dec(22), dec(24)));
    }

    @Test
    void periodTouchingOnOneDayShouldCountAsOverlap() {
        saveAbsence(prof, 21, 23, AbsenceStatus.OPEN);

        // Beginnt genau am letzten Tag des bestehenden Eintrags. Beide Grenzen zählen mit
        // (<= und >=), der 23.12. wäre also doppelt gebucht.
        assertTrue(absenceRepository.existsOverlap(prof, dec(23), dec(25)));
    }

    @Test
    void directlyFollowingPeriodShouldNotCountAsOverlap() {
        saveAbsence(prof, 21, 23, AbsenceStatus.OPEN);

        // Beginnt am Tag danach, kein Tag ist doppelt
        assertFalse(absenceRepository.existsOverlap(prof, dec(24), dec(26)));
    }

    @Test
    void rejectedAbsenceShouldNotBlockThePeriod() {
        saveAbsence(prof, 21, 23, AbsenceStatus.REJECTED);

        // Abgelehnte Einträge zählen nicht (status <> REJECTED in der Abfrage)
        assertFalse(absenceRepository.existsOverlap(prof, dec(21), dec(23)));
    }

    @Test
    void absenceOfAnotherUserShouldNotBlockThePeriod() {
        User colleague = entityManager.persist(
                new User(UUID.randomUUID(), "Erika", "Musterfrau", "erika@example.com"));
        saveAbsence(colleague, 21, 23, AbsenceStatus.APPROVED);

        // Kollegen dürfen gleichzeitig abwesend sein, geprüft wird nur dieselbe Person
        assertFalse(absenceRepository.existsOverlap(prof, dec(21), dec(23)));
    }

    @Test
    void absenceShouldNotOverlapWithItselfWhenUpdating() {
        Absence existing = saveAbsence(prof, 21, 23, AbsenceStatus.OPEN);

        // Beim Ändern (PUT) wird der Eintrag selbst ausgenommen, sonst wäre jede
        // Verschiebung eine "Überschneidung mit sich selbst".
        assertFalse(absenceRepository.existsOverlapExcluding(prof, dec(22), dec(24), existing.getId()));
        // Ein zweiter Eintrag im selben Zeitraum wird trotzdem gefunden
        assertTrue(absenceRepository.existsOverlapExcluding(prof, dec(22), dec(24), existing.getId() + 1));
    }

    @Test
    void databaseConstraintShouldRejectOverlapEvenWithoutCheckInCode() {
        saveAbsence(prof, 21, 23, AbsenceStatus.APPROVED);

        // Wir umgehen absichtlich existsOverlap und speichern direkt. Dann greift der
        // Constraint absence_no_overlap (EXCLUDE USING gist) in PostgreSQL. Spring übersetzt
        // den Fehler der Datenbank in eine DataIntegrityViolationException.
        // Genau das passiert in echt, wenn zwei Anfragen gleichzeitig kommen.
        Absence overlapping = new Absence(prof, prof, dec(22), dec(24), null, AbsenceStatus.OPEN);
        assertThrows(DataIntegrityViolationException.class,
                () -> absenceRepository.saveAndFlush(overlapping));
    }

    // ---------------------------------------------------------------------------------------
    // Abfragen für den Kalender
    // ---------------------------------------------------------------------------------------

    @Test
    void findInPeriodShouldReturnOnlyAbsencesInPeriodSortedByStart() {
        saveAbsence(prof, 28, 30, AbsenceStatus.OPEN);
        saveAbsence(prof, 14, 16, AbsenceStatus.APPROVED);
        saveAbsence(prof, 1, 3, AbsenceStatus.APPROVED);   // liegt vor dem Zeitraum

        // Kalenderansicht vom 10.12. bis 31.12.
        List<Absence> result = absenceRepository.findInPeriod(dec(10), dec(31));

        assertEquals(2, result.size());
        assertEquals(dec(14), result.get(0).getStartDate());
        assertEquals(dec(28), result.get(1).getStartDate());
    }

    @Test
    void findInPeriodWithStatusShouldFilterByStatus() {
        saveAbsence(prof, 14, 16, AbsenceStatus.APPROVED);
        saveAbsence(prof, 28, 30, AbsenceStatus.OPEN);

        List<Absence> result = absenceRepository.findInPeriodWithStatus(dec(1), dec(31), AbsenceStatus.OPEN);

        assertEquals(1, result.size());
        assertEquals(AbsenceStatus.OPEN, result.get(0).getStatus());
    }

    // ---------------------------------------------------------------------------------------
    // Hilfsmethoden, damit die Tests kurz und lesbar bleiben
    // ---------------------------------------------------------------------------------------

    /** Speichert eine Abwesenheit im Dezember 2026 vom Tag "from" bis zum Tag "to". */
    private Absence saveAbsence(User user, int from, int to, AbsenceStatus status) {
        Absence absence = absenceRepository.save(new Absence(user, user, dec(from), dec(to), null, status));
        entityManager.flush();
        return absence;
    }

    /** Ein Tag im Dezember 2026. */
    private static LocalDate dec(int day) {
        return LocalDate.of(2026, 12, day);
    }
}
