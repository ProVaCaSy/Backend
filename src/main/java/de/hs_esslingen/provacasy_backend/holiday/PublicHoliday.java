package de.hs_esslingen.provacasy_backend.holiday;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Gesetzlicher Feiertag in Baden-Württemberg (Tabelle public_holiday aus V1__init.sql).
 * Zwischenspeicher der Feiertags-API, dazu der 24.12. und 31.12. als ganze Feiertage.
 * Hat keine Beziehung zu anderen Tabellen.
 */
// Die einfachste Entity im Projekt: kein @GeneratedValue, keine Beziehungen, kein @Version.
@Entity
@Table(name = "public_holiday")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublicHoliday {

    // Kein @GeneratedValue: Den Schlüssel (das Datum) geben wir selbst vor, nicht die Datenbank.
    /** Das Datum ist der Primärschlüssel, jeder Tag kommt nur einmal vor. */
    @Id
    @Column(name = "holiday_date", nullable = false, updatable = false)
    private LocalDate holidayDate;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    public PublicHoliday(LocalDate holidayDate, String name) {
        this.holidayDate = holidayDate;
        this.name = name;
    }
}
