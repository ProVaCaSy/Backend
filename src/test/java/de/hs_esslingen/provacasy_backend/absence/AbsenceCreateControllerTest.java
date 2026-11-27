package de.hs_esslingen.provacasy_backend.absence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.hs_esslingen.provacasy_backend.dto.AbsenceCreateRequest;
import de.hs_esslingen.provacasy_backend.exception.AbsenceOverlapException;
import de.hs_esslingen.provacasy_backend.exception.InvalidRequestException;
import de.hs_esslingen.provacasy_backend.security.SecurityConfig;
import de.hs_esslingen.provacasy_backend.security.KeycloakJwtAuthenticationConverter;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AbsenceController.class)
@Import({
    SecurityConfig.class,
    KeycloakJwtAuthenticationConverter.class
})
class AbsenceCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AbsenceService absenceService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final String VALID_REQUEST = """
            {
                "start_date": "2026-12-01",
                "end_date": "2026-12-04",
                "title": "Vacation"
            }
            """;

    @ParameterizedTest
    @ValueSource(strings = {
        "PROFESSOR", "SECRETARIAT", "DEAN"
    })
    void shouldCreateAbsence(String role) throws Exception {

        var dto =
                new de.hs_esslingen.provacasy_backend.dto.Absence();

        dto.setId(42L);
        dto.setTitle("Vacation");

        when(absenceService.createAbsence(
                any(AbsenceCreateRequest.class),
                any(JwtAuthenticationToken.class)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/absences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST)
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_" + role))))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/absences/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").value("Vacation"));

        verify(absenceService).createAbsence(
                any(AbsenceCreateRequest.class),
                any(JwtAuthenticationToken.class));
    }

    @Test
    void shouldReturnConflictForOverlappingAbsence()
            throws Exception {

        when(absenceService.createAbsence(
                any(AbsenceCreateRequest.class),
                any(JwtAuthenticationToken.class)))
                .thenThrow(new AbsenceOverlapException(
                        "The absence overlaps with an existing absence."
                ));

        mockMvc.perform(post("/api/absences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST)
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_PROFESSOR"))))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnForbiddenForInsufficientPermission()
            throws Exception {

        when(absenceService.createAbsence(
                any(AbsenceCreateRequest.class),
                any(JwtAuthenticationToken.class)))
                .thenThrow(new AccessDeniedException(
                        "You may only create absences for yourself."
                ));

        mockMvc.perform(post("/api/absences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST)
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_PROFESSOR"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldRejectMissingRequiredDates() throws Exception {

        String request = """
                {
                    "title": "Vacation"
                }
                """;

        mockMvc.perform(post("/api/absences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_PROFESSOR"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(absenceService);
    }

    @Test
    void shouldRejectInvalidDateRange() throws Exception {

        String request = """
                {
                    "start_date": "2026-12-04",
                    "end_date": "2026-12-01",
                    "title": "Vacation"
                }
                """;

        when(absenceService.createAbsence(
                any(AbsenceCreateRequest.class),
                any(JwtAuthenticationToken.class)))
                .thenThrow(new InvalidRequestException(
                        "End date must not be before start date."
                ));

        mockMvc.perform(post("/api/absences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_PROFESSOR"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(post("/api/absences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(absenceService);
    }

    @Test
    void shouldRejectStudent() throws Exception {

        mockMvc.perform(post("/api/absences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST)
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(absenceService);
    }
}