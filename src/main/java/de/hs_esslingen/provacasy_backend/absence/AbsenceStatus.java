package de.hs_esslingen.provacasy_backend.absence;

/**
 * Status einer Abwesenheit.
 * Die Namen müssen genau zu den Werten des PostgreSQL Typs absence_status aus V1__init.sql passen.
 */
public enum AbsenceStatus {

    /** Vom Professor für sich selbst eingetragen, das Dekanat hat noch nicht entschieden. */
    OPEN,

    /** Vom Dekanat genehmigt, oder vom Sekretariat bzw. Dekanat für eine andere Person eingetragen. */
    APPROVED,

    /** Vom Dekanat abgelehnt. Zählt nicht als Urlaub und blockiert den Zeitraum nicht. */
    REJECTED
}