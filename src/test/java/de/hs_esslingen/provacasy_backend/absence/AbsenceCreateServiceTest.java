package de.hs_esslingen.provacasy_backend.absence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import de.hs_esslingen.provacasy_backend.dto.AbsenceCreateRequest;
import de.hs_esslingen.provacasy_backend.exception.AbsenceOverlapException;
import de.hs_esslingen.provacasy_backend.exception.InvalidRequestException;
import de.hs_esslingen.provacasy_backend.user.User;
import de.hs_esslingen.provacasy_backend.user.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbsenceCreateServiceTest {

    private final AbsenceRepository repository =
            mock(AbsenceRepository.class);

    private final AbsenceMapper mapper =
            mock(AbsenceMapper.class);

    private final UserService userService =
            mock(UserService.class);

    private final AbsenceService service =
            new AbsenceService(repository, mapper, userService);

    private final UUID currentUserId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    private final User currentUser = new User(
            currentUserId,
            "Max",
            "Mustermann",
            "max@example.com"
    );

    private final User otherUser = new User(
            otherUserId,
            "Anna",
            "Musterfrau",
            "anna@example.com"
    );

    private final LocalDate startDate =
            LocalDate.of(2026, 12, 1);

    private final LocalDate endDate =
            LocalDate.of(2026, 12, 4);

    @ParameterizedTest
    @ValueSource(strings = {
        "PROFESSOR", "SECRETARIAT", "DEAN"
    })
    void shouldCreateOwnAbsenceWithOpenStatus(String role) {

        var authentication = authentication(role);
        var request = createRequest(null);

        when(userService.syncCurrentUser(authentication.getToken()))
                .thenReturn(currentUser);

        var dto =
                new de.hs_esslingen.provacasy_backend.dto.Absence();

        when(repository.saveAndFlush(any(Absence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toDto(any(Absence.class)))
                .thenReturn(dto);

        var result = service.createAbsence(request, authentication);

        ArgumentCaptor<Absence> captor =
                ArgumentCaptor.forClass(Absence.class);

        verify(repository).saveAndFlush(captor.capture());

        Absence saved = captor.getValue();

        assertEquals(currentUser, saved.getUser());
        assertEquals(currentUser, saved.getCreatedFrom());
        assertEquals(AbsenceStatus.OPEN, saved.getStatus());
        assertEquals(startDate, saved.getStartDate());
        assertEquals(endDate, saved.getEndDate());
        assertEquals("Vacation", saved.getTitle());
        assertSame(dto, result);

        verify(userService, never()).findById(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SECRETARIAT", "DEAN"
    })
    void shouldCreateAbsenceForOtherUserWithApprovedStatus(
            String role) {

        var authentication = authentication(role);
        var request = createRequest(otherUserId);

        when(userService.syncCurrentUser(authentication.getToken()))
                .thenReturn(currentUser);

        when(userService.findById(otherUserId))
                .thenReturn(otherUser);

        when(repository.saveAndFlush(any(Absence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.createAbsence(request, authentication);

        ArgumentCaptor<Absence> captor =
                ArgumentCaptor.forClass(Absence.class);

        verify(repository).saveAndFlush(captor.capture());

        Absence saved = captor.getValue();

        assertEquals(otherUser, saved.getUser());
        assertEquals(currentUser, saved.getCreatedFrom());
        assertEquals(AbsenceStatus.APPROVED, saved.getStatus());

        verify(repository).existsOverlap(
                otherUser, startDate, endDate);
    }

    @Test
    void shouldRejectProfessorCreatingAbsenceForOtherUser() {

        var request = createRequest(otherUserId);

        assertThrows(
                AccessDeniedException.class,
                () -> service.createAbsence(
                        request, authentication("PROFESSOR"))
        );

        verifyNoInteractions(repository, mapper, userService);
    }

    @Test
    void shouldRejectOverlappingAbsence() {

        var authentication = authentication("PROFESSOR");
        var request = createRequest(null);

        when(userService.syncCurrentUser(authentication.getToken()))
                .thenReturn(currentUser);

        when(repository.existsOverlap(
                currentUser, startDate, endDate))
                .thenReturn(true);

        assertThrows(
                AbsenceOverlapException.class,
                () -> service.createAbsence(request, authentication)
        );

        verify(repository, never()).saveAndFlush(any(Absence.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldRejectInvalidDateRange() {

        var request = createRequest(null);

        request.setStartDate(endDate);
        request.setEndDate(startDate);

        assertThrows(
                InvalidRequestException.class,
                () -> service.createAbsence(
                        request, authentication("PROFESSOR"))
        );

        verifyNoInteractions(repository, mapper, userService);
    }

    @Test
    void shouldRejectUnknownUser() {

        var authentication = authentication("DEAN");
        var request = createRequest(otherUserId);

        when(userService.syncCurrentUser(authentication.getToken()))
                .thenReturn(currentUser);

        when(userService.findById(otherUserId))
                .thenThrow(new InvalidRequestException(
                        "User was not found."));

        assertThrows(
                InvalidRequestException.class,
                () -> service.createAbsence(request, authentication)
        );

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void shouldAllowSingleDayAbsence() {

        var authentication = authentication("PROFESSOR");
        var request = createRequest(null);

        request.setEndDate(startDate);

        when(userService.syncCurrentUser(authentication.getToken()))
                .thenReturn(currentUser);

        when(repository.saveAndFlush(any(Absence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.createAbsence(request, authentication);

        ArgumentCaptor<Absence> captor =
                ArgumentCaptor.forClass(Absence.class);

        verify(repository).saveAndFlush(captor.capture());

        assertEquals(
                captor.getValue().getStartDate(),
                captor.getValue().getEndDate()
        );
    }

    private AbsenceCreateRequest createRequest(UUID userId) {

        AbsenceCreateRequest request = new AbsenceCreateRequest();

        request.setUserId(userId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setTitle("Vacation");

        return request;
    }

    private JwtAuthenticationToken authentication(String role) {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(currentUserId.toString())
                .claim("given_name", "Max")
                .claim("family_name", "Mustermann")
                .claim("email", "max@example.com")
                .build();

        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}