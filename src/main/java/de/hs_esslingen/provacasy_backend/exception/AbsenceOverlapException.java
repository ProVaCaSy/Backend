package de.hs_esslingen.provacasy_backend.exception;

public class AbsenceOverlapException extends ConflictException {

    public AbsenceOverlapException(String message) {
        super(message);
    }
}