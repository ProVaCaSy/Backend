package de.hs_esslingen.provacasy_backend.exception;

import java.sql.SQLException;
import java.net.URI;

import jakarta.persistence.OptimisticLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidRequestException.class)
    public ProblemDetail handleInvalidRequest(
            InvalidRequestException ex) {

        return createProblem(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(
            ResourceNotFoundException ex) {

        return createProblem(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(
            ConflictException ex) {

        return createProblem(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(
            AccessDeniedException ex) {

        return createProblem(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action."
        );
    }

    @ExceptionHandler({
        OptimisticLockingFailureException.class,
        OptimisticLockException.class
    })
    public ProblemDetail handleOptimisticLock() {

        return createProblem( 
                HttpStatus.CONFLICT,
                "The resource has been modified. Please reload and try again."
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        Throwable cause = ex;

        while (cause != null) {
            if (cause instanceof SQLException sqlException
                    && "23P01".equals(sqlException.getSQLState())) {

                return handleConflict(new AbsenceOverlapException(
                        "The absence overlaps with an existing absence."
                ));
            }

            cause = cause.getCause();
        }

        log.error("Unexpected database constraint violation", ex);

        return createProblem( 
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected database error occurred."
        );
    }

    private ProblemDetail createProblem(
            HttpStatus status, String detail) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setType(URI.create("about:blank"));

        return problem;
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        if (body instanceof ProblemDetail problem
                && problem.getType() == null) {

            problem.setType(URI.create("about:blank"));
        }

        return super.createResponseEntity(
                body, headers, statusCode, request);
    }
}