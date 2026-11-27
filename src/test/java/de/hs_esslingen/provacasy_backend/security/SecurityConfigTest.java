
package de.hs_esslingen.provacasy_backend.security;

import de.hs_esslingen.provacasy_backend.user.UserController;
import de.hs_esslingen.provacasy_backend.user.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(
    value = UserController.class,
    properties = "app.cors.allowed-origins=https://provacasy.hs-esslingen.com"
)
@Import({
    SecurityConfig.class,
    KeycloakJwtAuthenticationConverter.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void withoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentShouldNotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/absences")
                .with(jwt().authorities()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.detail").value(
                        "You do not have permission to perform this action."))
                .andExpect(jsonPath("$.instance").value("/api/absences"));
    }

    @Test
    void configuredOriginShouldBeAllowed() throws Exception {
        mockMvc.perform(options("/api/users/me")
                .header(HttpHeaders.ORIGIN,
                        "https://provacasy.hs-esslingen.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                        "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://provacasy.hs-esslingen.com"));
    }

    @Test
    void unknownOriginShouldBeRejected() throws Exception {
        mockMvc.perform(options("/api/users/me")
                .header(HttpHeaders.ORIGIN, "https://example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void defaultOriginShouldBeRejectedWhenOverridden() throws Exception {
        mockMvc.perform(options("/api/users/me")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
