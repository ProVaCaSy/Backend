package de.hs_esslingen.provacasy_backend.entitlement;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import de.hs_esslingen.provacasy_backend.user.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Urlaubsanspruch eines Benutzers in einem Jahr (Tabelle vacation_entitlement aus V1__init.sql).
 * Standard sind 30 Tage, das Sekretariat kann den Wert für das laufende Jahr ändern.
 */
@Entity
@Table(name = "vacation_entitlement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VacationEntitlement {

    /** Primärschlüssel aus user_id und year. */
    @EmbeddedId
    private VacationEntitlementId id;

    /** Wessen Anspruch das ist (Beziehung "hat"). Füllt den Teil userId des Schlüssels. */
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    /** Anzahl der Urlaubstage, nicht negativ (Constraint vacation_entitlement_days_check). */
    @Setter
    @Column(name = "days", nullable = false)
    private Short days;

    public VacationEntitlement(User user, short year, short days) {
        this.id = new VacationEntitlementId(user.getId(), year);
        this.user = user;
        this.days = days;
    }

    /** Das Jahr aus dem Schlüssel, damit man nicht immer getId().getYear() schreiben muss. */
    public Short getYear() {
        return id.getYear();
    }
}
