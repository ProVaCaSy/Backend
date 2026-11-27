package de.hs_esslingen.provacasy_backend.entitlement;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Zusammengesetzter Primärschlüssel von vacation_entitlement: Benutzer und Jahr.
 * Pro Benutzer und Jahr gibt es genau einen Urlaubsanspruch.
 */
// Warum eine eigene Klasse? JPA erlaubt pro Entity nur ein @Id Feld. Besteht der
// Primärschlüssel aus zwei Spalten (user_id, year), packt man beide in eine Klasse.
// @Embeddable: Diese Klasse ist keine eigene Tabelle, ihre Felder werden in die Tabelle der
// Entity "eingebettet" (dort mit @EmbeddedId verwendet).
@Embeddable
@Getter
// Für Hibernate, wie bei jeder Entity.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// Für uns: new VacationEntitlementId(userId, year), z. B. für findById(...).
@AllArgsConstructor
// Pflicht für Schlüsselklassen: Zwei Schlüssel mit gleicher userId und gleichem year müssen
// als gleich gelten. Sonst findet Hibernate einen schon geladenen Eintrag nicht wieder.
// Lombok schreibt equals() und hashCode() aus allen Feldern.
@EqualsAndHashCode
// Serializable verlangt die JPA Spezifikation für Schlüsselklassen.
public class VacationEntitlementId implements Serializable {

    // Versionsnummer für Serializable, sonst warnt der Compiler bzw. Sonar.
    private static final long serialVersionUID = 1L;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "year", nullable = false)
    private Short year;
}
