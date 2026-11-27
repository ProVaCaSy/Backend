package de.hs_esslingen.provacasy_backend.absence;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

    // GET: List absences
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

    // POST: Create absence
    @Override
    @PreAuthorize("hasAnyRole('PROFESSOR', 'SECRETARIAT', 'DEAN')")
    public ResponseEntity<Absence> createAbsence(
            AbsenceCreateRequest request) {

        JwtAuthenticationToken authentication =
                getCurrentAuthentication();

        Absence createdAbsence =
                absenceService.createAbsence(request, authentication);

        URI location = URI.create(
                "/api/absences/" + createdAbsence.getId()
        );

        return ResponseEntity
                .created(location)
                .body(createdAbsence);
    }

    // PUT: Update absence
    @Override
    public ResponseEntity<Absence> updateAbsence(
            Long id,
            AbsenceUpdateRequest request) {

        throw notImplemented();
    }

    // DELETE: Delete absence
    @Override
    public ResponseEntity<Void> deleteAbsence(Long id) {

        throw notImplemented();
    }

    // POST: Approve absence
    @Override
    @PreAuthorize("hasRole('DEAN')")
    public ResponseEntity<Absence> approveAbsence(Long id) {

        throw notImplemented();
    }

    // POST: Reject absence
    @Override
    @PreAuthorize("hasRole('DEAN')")
    public ResponseEntity<Absence> rejectAbsence(Long id) {

        throw notImplemented();
    }

    // POST: Approve all absences
    @Override
    @PreAuthorize("hasRole('DEAN')")
    public ResponseEntity<List<Absence>> approveAllAbsences() {

        throw notImplemented();
    }

    // GET: Download ICS file
    @Override
    public ResponseEntity<String> downloadAbsenceIcs(Long id) {

        throw notImplemented();
    }

    // Retrieve authenticated Keycloak user
    private JwtAuthenticationToken getCurrentAuthentication() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No authenticated user"
            );
        }

        return jwtAuth;
    }

    private ResponseStatusException notImplemented() {

        return new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "This operation is not implemented yet."
        );
    }
}