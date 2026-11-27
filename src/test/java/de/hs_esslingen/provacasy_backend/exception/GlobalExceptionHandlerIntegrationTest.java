package de.hs_esslingen.provacasy_backend.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @ParameterizedTest
    @CsvSource({
        "invalid, 400, Bad Request",
        "forbidden, 403, Forbidden",
        "missing, 404, Not Found",
        "conflict, 409, Conflict",
        "overlap, 409, Conflict",
        "version, 409, Conflict"
    })
    void shouldReturnProblemJson(
            String error, int expectedStatus, String expectedTitle
    ) throws Exception {

        mockMvc.perform(get("/api/test-errors/" + error))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.title").value(expectedTitle))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value(
                        "/api/test-errors/" + error));
    }

    @Test
    void malformedJsonShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/test-errors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void validationErrorShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/test-errors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @RestController
    static class TestController {

        @GetMapping("/api/test-errors/{error}")
        public void throwException(@PathVariable("error") String error) {

            throw switch (error) {
                case "invalid" ->
                    new InvalidRequestException("Invalid date range");

                case "forbidden" ->
                    new AccessDeniedException("Access denied");

                case "missing" ->
                    new ResourceNotFoundException("Absence not found");

                case "conflict" ->
                    new ConflictException("Absence already approved");

                case "overlap" ->
                    new AbsenceOverlapException("Overlapping absence");

                case "version" ->
                    new OptimisticLockException("Version conflict");

                default ->
                    new IllegalArgumentException("Unknown test case");
            };
        }

        @PostMapping("/api/test-errors")
        public void validateRequest(
                @Valid @RequestBody TestRequest request) {
        }
    }

    public record TestRequest(@NotBlank String title) {
    }
}