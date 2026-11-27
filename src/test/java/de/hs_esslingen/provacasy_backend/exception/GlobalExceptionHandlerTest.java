package de.hs_esslingen.provacasy_backend.exception;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void invalidRequestShouldReturn400() {
        ProblemDetail result = handler.handleInvalidRequest(
                new InvalidRequestException("Invalid date range")
        );

        assertEquals(400, result.getStatus());
        assertEquals("Invalid date range", result.getDetail());
    }

    @Test
    void resourceNotFoundShouldReturn404() {
        ProblemDetail result = handler.handleResourceNotFound(
                new ResourceNotFoundException("Absence not found")
        );

        assertEquals(404, result.getStatus());
        assertEquals("Absence not found", result.getDetail());
    }

    @Test
    void conflictShouldReturn409() {
        ProblemDetail result = handler.handleConflict(
                new ConflictException("Absence is already approved")
        );

        assertEquals(409, result.getStatus());
        assertEquals("Absence is already approved", result.getDetail());
    }

    @Test
    void absenceOverlapShouldReturn409() {
        ProblemDetail result = handler.handleConflict(
                new AbsenceOverlapException("Overlapping absence")
        );

        assertEquals(409, result.getStatus());
        assertEquals("Overlapping absence", result.getDetail());
    }

    @Test
    void accessDeniedShouldReturn403() {
        ProblemDetail result = handler.handleAccessDenied(
                new AccessDeniedException("Access denied")
        );

        assertEquals(403, result.getStatus());
        assertEquals(
                "You do not have permission to perform this action.",
                result.getDetail()
        );
    }

    @Test
    void optimisticLockShouldReturn409() {
        ProblemDetail result = handler.handleOptimisticLock();

        assertEquals(409, result.getStatus());
        assertEquals(
                "The resource has been modified. Please reload and try again.",
                result.getDetail()
        );
    }

    @Test
    void databaseOverlapShouldReturn409() {
        SQLException cause = new SQLException(
                "Exclusion constraint violated", "23P01"
        );

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Database conflict", cause
                );

        ProblemDetail result =
                handler.handleDataIntegrityViolation(exception);

        assertEquals(409, result.getStatus());
        assertEquals(
                "The absence overlaps with an existing absence.",
                result.getDetail()
        );
    }

    @Test
    void unexpectedDatabaseErrorShouldReturn500() {
        SQLException cause = new SQLException(
                "Unexpected database error", "23505"
        );

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Database error", cause
                );

        ProblemDetail result =
                handler.handleDataIntegrityViolation(exception);

        assertEquals(500, result.getStatus());
        assertEquals(
                "An unexpected database error occurred.",
                result.getDetail()
        );
    }
}