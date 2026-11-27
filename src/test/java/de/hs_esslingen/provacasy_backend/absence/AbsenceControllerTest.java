package de.hs_esslingen.provacasy_backend.absence;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.hs_esslingen.provacasy_backend.exception.InvalidRequestException;
import de.hs_esslingen.provacasy_backend.security.SecurityConfig;
import de.hs_esslingen.provacasy_backend.security.KeycloakJwtAuthenticationConverter;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AbsenceController.class)
@Import({
    SecurityConfig.class,
    KeycloakJwtAuthenticationConverter.class
})
class AbsenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AbsenceService absenceService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final LocalDate from = LocalDate.of(2026, 12, 1);
    private final LocalDate to = LocalDate.of(2026, 12, 31);

    @ParameterizedTest
    @ValueSource(strings = {
        "PROFESSOR", "SECRETARIAT", "DEAN"
    })
    void shouldAllowAllApplicationRoles(String role)
            throws Exception {

        var dto =
                new de.hs_esslingen.provacasy_backend.dto.Absence();

        dto.setId(42L);

        when(absenceService.listAbsences(from, to, null))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/absences")
                .param("from", from.toString())
                .param("to", to.toString())
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_" + role))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(42));
    }

    @Test
    void shouldFilterByStatus() throws Exception {

        var status =
                de.hs_esslingen.provacasy_backend.dto.AbsenceStatus.OPEN;

        when(absenceService.listAbsences(from, to, status))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/absences")
                .param("from", from.toString())
                .param("to", to.toString())
                .param("status", "OPEN")
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_DEAN"))))
                .andExpect(status().isOk());

        verify(absenceService).listAbsences(from, to, status);
    }

    @Test
    void shouldRejectInvalidDateRange() throws Exception {

        when(absenceService.listAbsences(to, from, null))
                .thenThrow(new InvalidRequestException(
                        "End date must not be before start date."));

        mockMvc.perform(get("/api/absences")
                .param("from", to.toString())
                .param("to", from.toString())
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_PROFESSOR"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldRejectMissingParameter() throws Exception {

        mockMvc.perform(get("/api/absences")
                .param("from", from.toString())
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_PROFESSOR"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(absenceService);
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(get("/api/absences")
                .param("from", from.toString())
                .param("to", to.toString()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(absenceService);
    }

    @Test
    void shouldRejectStudent() throws Exception {

        mockMvc.perform(get("/api/absences")
                .param("from", from.toString())
                .param("to", to.toString())
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(absenceService);
    }
}