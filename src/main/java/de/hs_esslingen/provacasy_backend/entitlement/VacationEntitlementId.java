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
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class VacationEntitlementId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "year", nullable = false)
    private Short year;
}
