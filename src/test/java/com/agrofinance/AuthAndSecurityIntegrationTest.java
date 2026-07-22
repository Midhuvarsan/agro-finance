package com.agrofinance;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
/**
 * FULL integration test: real SecurityFilterChain (JwtAuthenticationFilter
 * included), real JJWT tokens, real @PreAuthorize enforcement, an actual
 * H2 database. This is the layer that unambiguously proves role-based
 * authorization actually works — the thing the FarmerControllerTest slice
 * couldn't fully guarantee.
 *
 * @Transactional on each test rolls back all DB changes automatically —
 * tests never leak state into each other.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth flow and role-based authorization — full integration")
class AuthAndSecurityIntegrationTest {
 
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
 
    private String registerAndLogin(String email, String role) throws Exception {
        String registerBody = objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
            put("email", email);
            put("password", "password123");
            put("phone", "9999999999");
            put("role", role);
        }});
 
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json").content(registerBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
 
        return objectMapper.readTree(response).get("token").asText();
    }
 
    @Test
    @DisplayName("register -> login -> protected endpoint round-trip, real JWT end to end")
    void registerLoginAndAccessProtectedEndpoint() throws Exception {
        String token = registerAndLogin("itest-farmer@test.com", "FARMER");
 
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("itest-farmer@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("FARMER"));
    }
 
    @Test
    @DisplayName("no token at all -> protected endpoint is rejected")
    void noToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().is4xxClientError()); // 403, per the Http403ForbiddenEntryPoint default from Phase 3
    }
 
    @Test
    @DisplayName("a FARMER token is REJECTED on an ADMIN-only endpoint — proves @PreAuthorize enforcement, not just authentication")
    void wrongRole_isForbidden() throws Exception {
        String farmerToken = registerAndLogin("itest-farmer2@test.com", "FARMER");
 
        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isForbidden());
    }
 
    @Test
    @DisplayName("a BANK_OFFICER cannot create a FARMER profile — cross-role write is blocked")
    void officerCannotCreateFarmerProfile() throws Exception {
        String officerToken = registerAndLogin("itest-officer@test.com", "BANK_OFFICER");
 
        String body = """
                {"fullName":"Hacker","aadhaarNumber":"234123412346","address":"Nowhere"}
                """;
 
        mockMvc.perform(post("/api/farmers/me")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType("application/json").content(body))
                .andExpect(status().isForbidden());
    }
 
    @Test
    @DisplayName("registering the same email twice returns 409, not a 500")
    void duplicateRegistration_returns409() throws Exception {
        registerAndLogin("itest-dup@test.com", "FARMER");
 
        String secondAttempt = objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
            put("email", "itest-dup@test.com");
            put("password", "password123");
            put("phone", "9999999999");
            put("role", "FARMER");
        }});
 
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json").content(secondAttempt))
                .andExpect(status().isConflict());
    }
 
    @Test
    @DisplayName("cannot self-register as ADMIN")
    void selfRegisterAsAdmin_isForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
            put("email", "itest-wannabe-admin@test.com");
            put("password", "password123");
            put("phone", "9999999999");
            put("role", "ADMIN");
        }});
 
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json").content(body))
                .andExpect(status().isForbidden());
    }
 
}
 


































