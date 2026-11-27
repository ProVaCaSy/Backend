package de.hs_esslingen.provacasy_backend.absence;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.hs_esslingen.provacasy_backend.exception.InvalidRequestException;
import de.hs_esslingen.provacasy_backend.user.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbsenceServiceTest {

    private final AbsenceRepository repository =
            mock(AbsenceRepository.class);

    private final AbsenceMapper mapper =
            mock(AbsenceMapper.class);

    private final UserService userService =
            mock(UserService.class);

    private final AbsenceService service =
            new AbsenceService(repository, mapper, userService);

    private final LocalDate from = LocalDate.of(2026, 12, 1);
    private final LocalDate to = LocalDate.of(2026, 12, 31);

    @Test
    void shouldListAbsencesWithoutStatusFilter() {

        Absence entity = mock(Absence.class);

        var dto =
                new de.hs_esslingen.provacasy_backend.dto.Absence();

        var entities = List.of(entity);

        when(repository.findInPeriod(from, to))
                .thenReturn(entities);

        when(mapper.toDtoList(entities))
                .thenReturn(List.of(dto));

        var result = service.listAbsences(from, to, null);

        assertEquals(List.of(dto), result);

        verify(repository).findInPeriod(from, to);
        verify(mapper).toDtoList(entities);
    }

    @Test
    void shouldListAbsencesWithStatusFilter() {

        when(repository.findInPeriodWithStatus(
                from, to, AbsenceStatus.OPEN))
                .thenReturn(List.of());

        when(mapper.toDtoList(List.of()))
                .thenReturn(List.of());

        var result = service.listAbsences(
                from,
                to,
                de.hs_esslingen.provacasy_backend.dto.AbsenceStatus.OPEN
        );

        assertTrue(result.isEmpty());

        verify(repository).findInPeriodWithStatus(
                from, to, AbsenceStatus.OPEN);
    }

    @Test
    void shouldRejectInvalidDateRange() {

        assertThrows(
                InvalidRequestException.class,
                () -> service.listAbsences(to, from, null)
        );

        verifyNoInteractions(repository, mapper, userService);
    }

    @Test
    void shouldRejectMissingDates() {

        assertThrows(
                InvalidRequestException.class,
                () -> service.listAbsences(null, to, null)
        );

        assertThrows(
                InvalidRequestException.class,
                () -> service.listAbsences(from, null, null)
        );

        verifyNoInteractions(repository, mapper, userService);
    }

    @Test
    void shouldAllowSingleDayPeriod() {

        when(repository.findInPeriod(from, from))
                .thenReturn(List.of());

        when(mapper.toDtoList(List.of()))
                .thenReturn(List.of());

        var result = service.listAbsences(from, from, null);

        assertTrue(result.isEmpty());

        verify(repository).findInPeriod(from, from);
    }
}