package de.hs_esslingen.provacasy_backend.absence;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import de.hs_esslingen.provacasy_backend.user.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Eine Abwesenheit im Kalender (Tabelle absence aus V1__init.sql).
 */
@Entity
@Table(name = "absence")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Setter
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Setter
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Setter
    @Column(name = "title")
    private String title;

    /** Wer abwesend ist (Beziehung "betrifft"). Kann nach dem Anlegen nicht geändert werden. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    /** Wer den Eintrag angelegt hat (Beziehung "erfasst von"): die Person selbst, das Sekretariat oder das Dekanat. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_from", nullable = false, updatable = false)
    private User createdFrom;

    /** Wer den Eintrag zuletzt geändert hat (Beziehung "geändert von"). Leer, solange er unverändert ist. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_from")
    private User updatedFrom;

    /** Offen, genehmigt oder abgelehnt. In der Datenbank der eigene Typ absence_status. */
    @Setter
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private AbsenceStatus status;

    /** Steigt bei jeder Änderung um 1 (optimistisches Sperren) und ist die SEQUENCE der ICS Datei. */
    @Version
    @Column(name = "version", nullable = false)
    private Short version;

    /** Zeitpunkt des Anlegens, wird beim ersten Speichern automatisch gesetzt. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Zeitpunkt der letzten Änderung, wird nur zusammen mit updatedFrom gesetzt. */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Legt eine neue Abwesenheit an.
     * Den Status bestimmt der Service: OPEN, wenn ein Professor für sich selbst einträgt,
     * APPROVED, wenn Sekretariat oder Dekanat für eine andere Person eintragen.
     */
    public Absence(User user, User createdFrom, LocalDate startDate, LocalDate endDate,
                   String title, AbsenceStatus status) {
        this.user = user;
        this.createdFrom = createdFrom;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.status = status;
    }

    /**
     * Merkt sich, wer den Eintrag wann zuletzt geändert hat.
     * updated_from und updated_at werden immer zusammen gesetzt (Constraint absence_updated_check).
     */
    public void markUpdated(User updatedFrom) {
        this.updatedFrom = updatedFrom;
        this.updatedAt = Instant.now();
    }
}
