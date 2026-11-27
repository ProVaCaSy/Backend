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

    // @EmbeddedId: Der Primärschlüssel ist ein Objekt mit zwei Feldern (siehe VacationEntitlementId).
    /** Primärschlüssel aus user_id und year. */
    @EmbeddedId
    private VacationEntitlementId id;

    // Problem: Die Spalte user_id gehört zweimal hierher, einmal als Teil des Schlüssels und
    // einmal als Fremdschlüssel auf app_user. @MapsId("userId") sagt Hibernate: Das ist dieselbe
    // Spalte, nimm die ID des Users als userId im Schlüssel. So gibt es keine doppelte Spalte.
    /** Wessen Anspruch das ist (Beziehung "hat"). Füllt den Teil userId des Schlüssels. */
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    // Short, weil die Spalte smallint ist. Nur days hat einen Setter: Benutzer und Jahr
    // sind der Schlüssel und ändern sich nie.
    /** Anzahl der Urlaubstage, nicht negativ (Constraint vacation_entitlement_days_check). */
    @Setter
    @Column(name = "days", nullable = false)
    private Short days;

    // short (klein) statt Short: Hier darf kein null übergeben werden.
    public VacationEntitlement(User user, short year, short days) {
        this.id = new VacationEntitlementId(user.getId(), year);
        this.user = user;
        this.days = days;
    }

    /** Das Jahr aus dem Schlüssel, damit man nicht immer getId().getYear() schreiben muss. */
    // Nebeneffekt: MapStruct findet diesen Getter und mappt year automatisch ins DTO.
    public Short getYear() {
        return id.getYear();
    }
}
