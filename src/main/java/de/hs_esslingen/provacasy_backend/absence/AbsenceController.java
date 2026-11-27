package de.hs_esslingen.provacasy_backend.absence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.hs_esslingen.provacasy_backend.api.AbsencesApi;
import de.hs_esslingen.provacasy_backend.dto.Absence;
import de.hs_esslingen.provacasy_backend.dto.AbsenceStatus;
import de.hs_esslingen.provacasy_backend.dto.AbsenceCreateRequest;
import de.hs_esslingen.provacasy_backend.dto.AbsenceUpdateRequest;

@RestController
@RequestMapping("/api")
public class AbsenceController implements AbsencesApi {

    private final AbsenceService absenceService;

    public AbsenceController(AbsenceService absenceService) {
        this.absenceService = absenceService;
    }

    @Override
    @PreAuthorize("hasAnyRole('PROFESSOR', 'SECRETARIAT', 'DEAN')")
    public ResponseEntity<List<Absence>> listAbsences(
            LocalDate from,
            LocalDate to,
            AbsenceStatus status) {

        return ResponseEntity.ok(
                absenceService.listAbsences(from, to, status)
        );
    }

    @Override
    public ResponseEntity<Absence> createAbsence(
            AbsenceCreateRequest request) {

        throw notImplemented();
    }

    @Override
    public ResponseEntity<Absence> updateAbsence(
            Long id,
            AbsenceUpdateRequest request) {

        throw notImplemented();
    }

    @Override
    public ResponseEntity<Void> deleteAbsence(Long id) {

        throw notImplemented();
    }

    @Override
    @PreAuthorize("hasRole('DEAN')")
    public ResponseEntity<Absence> approveAbsence(Long id) {

        throw notImplemented();
    }

    @Override
    @PreAuthorize("hasRole('DEAN')")
    public ResponseEntity<Absence> rejectAbsence(Long id) {

        throw notImplemented();
    }

    @Override
    @PreAuthorize("hasRole('DEAN')")
    public ResponseEntity<List<Absence>> approveAllAbsences() {

        throw notImplemented();
    }

    @Override
    public ResponseEntity<String> downloadAbsenceIcs(Long id) {

        throw notImplemented();
    }

    private ResponseStatusException notImplemented() {
        return new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "This operation is not implemented yet."
        );
    }
}