package de.hs_esslingen.provacasy_backend.entitlement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
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

// Aufbau wie AbsenceRepositoryTest: echtes PostgreSQL aus Testcontainers, Schema von Flyway,
// jeder Test in einer eigenen Transaktion, die am Ende zurückgerollt wird.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class VacationEntitlementRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private VacationEntitlementRepository entitlementRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User prof;

    @BeforeEach
    void setUp() {
        prof = entityManager.persist(new User(UUID.randomUUID(), "Max", "Mustermann", "max@example.com"));
    }

    @Test
    void entitlementShouldBeFoundByCompositeKey() {
        save(prof, 2026, 30);
        entityManager.clear();

        // Der Schlüssel besteht aus zwei Teilen, deshalb sucht man mit einem Schlüsselobjekt
        Optional<VacationEntitlement> loaded =
                entitlementRepository.findById(new VacationEntitlementId(prof.getId(), (short) 2026));

        assertTrue(loaded.isPresent());
        assertEquals((short) 30, loaded.get().getDays());
        assertEquals((short) 2026, loaded.get().getYear());
    }

    @Test
    void latestEntitlementBeforeYearShouldBeFound() {
        save(prof, 2024, 30);
        save(prof, 2025, 28);

        // Für 2027 gibt es noch keinen Eintrag, übernommen wird der jüngste davor (2025)
        Optional<VacationEntitlement> latest = entitlementRepository.findLatestBefore(prof.getId(), (short) 2027);

        assertTrue(latest.isPresent());
        assertEquals((short) 2025, latest.get().getYear());
        assertEquals((short) 28, latest.get().getDays());
    }

    @Test
    void sameYearShouldNotCountAsEarlierYear() {
        save(prof, 2026, 32);

        // "vor 2026" heißt streng kleiner, der Eintrag von 2026 selbst zählt nicht
        assertTrue(entitlementRepository.findLatestBefore(prof.getId(), (short) 2026).isEmpty());
    }

    @Test
    void entitlementOfAnotherUserShouldNotBeUsed() {
        User colleague = entityManager.persist(
                new User(UUID.randomUUID(), "Erika", "Musterfrau", "erika@example.com"));
        save(colleague, 2025, 35);

        // Ohne eigenen Eintrag kommt nichts zurück, der Service nimmt dann die Standard 30 Tage
        assertTrue(entitlementRepository.findLatestBefore(prof.getId(), (short) 2026).isEmpty());
    }

    @Test
    void allEntitlementsOfOneYearShouldBeFound() {
        User colleague = entityManager.persist(
                new User(UUID.randomUUID(), "Erika", "Musterfrau", "erika@example.com"));
        save(prof, 2026, 30);
        save(colleague, 2026, 25);
        save(prof, 2025, 30);   // anderes Jahr, gehört nicht dazu

        List<VacationEntitlement> result = entitlementRepository.findByIdYear((short) 2026);

        assertEquals(2, result.size());
    }

    @Test
    void negativeDaysShouldBeRejectedByDatabase() {
        // Constraint vacation_entitlement_days_check (days >= 0) in V1__init.sql
        VacationEntitlement invalid = new VacationEntitlement(prof, (short) 2026, (short) -1);

        assertThrows(DataIntegrityViolationException.class,
                () -> entitlementRepository.saveAndFlush(invalid));
    }

    /** Speichert einen Urlaubsanspruch und schickt ihn sofort an die Datenbank. */
    private void save(User user, int year, int days) {
        entitlementRepository.save(new VacationEntitlement(user, (short) year, (short) days));
        entityManager.flush();
    }
}
