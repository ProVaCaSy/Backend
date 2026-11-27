package de.hs_esslingen.provacasy_backend.absence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hs_esslingen.provacasy_backend.dto.AbsenceCreateRequest;
import de.hs_esslingen.provacasy_backend.exception.AbsenceOverlapException;
import de.hs_esslingen.provacasy_backend.exception.InvalidRequestException;
import de.hs_esslingen.provacasy_backend.user.User;
import de.hs_esslingen.provacasy_backend.user.UserService;

@Service
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final AbsenceMapper absenceMapper;
    private final UserService userService;

    public AbsenceService(
            AbsenceRepository absenceRepository,
            AbsenceMapper absenceMapper,
            UserService userService) {

        this.absenceRepository = absenceRepository;
        this.absenceMapper = absenceMapper;
        this.userService = userService;
    }

    // GET: List absences within a date range
    @Transactional(readOnly = true)
    public List<de.hs_esslingen.provacasy_backend.dto.Absence>
            listAbsences(
                    LocalDate from,
                    LocalDate to,
                    de.hs_esslingen.provacasy_backend.dto.AbsenceStatus status) {

        validateDateRange(from, to);

        List<Absence> absences;

        if (status == null) {

            absences = absenceRepository.findInPeriod(from, to);

        } else {

            AbsenceStatus entityStatus =
                    AbsenceStatus.valueOf(status.name());

            absences = absenceRepository.findInPeriodWithStatus(
                    from, to, entityStatus);
        }

        return absenceMapper.toDtoList(absences);
    }

    // POST: Create a new absence
    @Transactional
    public de.hs_esslingen.provacasy_backend.dto.Absence createAbsence(
            AbsenceCreateRequest request,
            JwtAuthenticationToken authentication) {

        validateDateRange(
                request.getStartDate(),
                request.getEndDate()
        );

        UUID currentUserId = UUID.fromString(
                authentication.getToken().getSubject()
        );

        UUID affectedUserId = request.getUserId() == null
                ? currentUserId
                : request.getUserId();

        boolean isOwnAbsence =
                currentUserId.equals(affectedUserId);

        boolean canCreateForOthers = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_SECRETARIAT")
                        || authority.getAuthority().equals("ROLE_DEAN"));

        // Professors may only create their own absences
        if (!isOwnAbsence && !canCreateForOthers) {

            throw new AccessDeniedException(
                    "You may only create absences for yourself."
            );
        }

        // Synchronize current user with PostgreSQL
        User currentUser = userService.syncCurrentUser(
                authentication.getToken()
        );

        // Determine the affected user
        User affectedUser = isOwnAbsence
                ? currentUser
                : userService.findById(affectedUserId);

        // Check for overlapping absences
        if (absenceRepository.existsOverlap(
                affectedUser,
                request.getStartDate(),
                request.getEndDate())) {

            throw new AbsenceOverlapException(
                    "The absence overlaps with an existing absence."
            );
        }

        // Determine initial status
        AbsenceStatus status = isOwnAbsence
                ? AbsenceStatus.OPEN
                : AbsenceStatus.APPROVED;

        // Create entity
        Absence absence = new Absence(
                affectedUser,
                currentUser,
                request.getStartDate(),
                request.getEndDate(),
                request.getTitle(),
                status
        );

        // Persist entity and check database constraints
        Absence savedAbsence =
                absenceRepository.saveAndFlush(absence);

        return absenceMapper.toDto(savedAbsence);
    }

    // Shared date validation
    private void validateDateRange(LocalDate from, LocalDate to) {

        if (from == null || to == null) {

            throw new InvalidRequestException(
                    "Start date and end date are required."
            );
        }

        if (to.isBefore(from)) {

            throw new InvalidRequestException(
                    "End date must not be before start date."
            );
        }
    }
}