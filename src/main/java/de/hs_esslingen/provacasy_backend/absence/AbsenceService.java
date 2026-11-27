package de.hs_esslingen.provacasy_backend.absence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hs_esslingen.provacasy_backend.exception.InvalidRequestException;

@Service
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final AbsenceMapper absenceMapper;

    public AbsenceService(
            AbsenceRepository absenceRepository,
            AbsenceMapper absenceMapper) {

        this.absenceRepository = absenceRepository;
        this.absenceMapper = absenceMapper;
    }

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

    private void validateDateRange(LocalDate from, LocalDate to) {

        if (from == null || to == null) {
            throw new InvalidRequestException(
                    "Start date and end date are required.");
        }

        if (to.isBefore(from)) {
            throw new InvalidRequestException(
                    "End date must not be before start date.");
        }
    }
}