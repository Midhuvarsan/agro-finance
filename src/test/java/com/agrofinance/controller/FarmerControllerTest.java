package com.agrofinance.controller;

import com.agrofinance.dto.FarmerRequest;
import com.agrofinance.dto.FarmerResponse;
import com.agrofinance.entity.User;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.service.DashboardService;
import com.agrofinance.service.FarmerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
 
import java.time.LocalDate;
 
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import com.agrofinance.entity.Role;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

 
/**
 * @WebMvcTest loads ONLY the web layer for this controller — fast, no
 * database, no real security filter chain. FarmerService/DashboardService
 * are mocked.
 *
 * IMPORTANT PITFALL, DELIBERATELY AVOIDED HERE: @WithMockUser creates a
 * generic Spring Security User as the principal — NOT our CustomUserDetails.
 * Every controller in this project does
 *     @AuthenticationPrincipal CustomUserDetails principal
 * so a test using @WithMockUser would compile fine but principal.getUser()
 * would be null/wrong-typed at runtime — a classic silent test-authoring bug.
 *
 * The fix: build a REAL CustomUserDetails and inject it via
 * SecurityMockMvcRequestPostProcessors.authentication(...) on each request,
 * so the controller receives exactly what it would in production.
 */
@WebMvcTest(
        controllers = FarmerController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.agrofinance.security.JwtAuthenticationFilter.class
        )
)
 
@DisplayName("FarmerController")
class FarmerControllerTest {
 
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
 
    @MockitoBean private FarmerService farmerService;
    @MockitoBean private DashboardService dashboardService;
 
    /** Builds a real, correctly-typed principal — this is the actual fix for the pitfall above. */
    private org.springframework.test.web.servlet.request.RequestPostProcessor farmerAuth(Long userId) {
        Role farmerRole = new Role();
        farmerRole.setName("FARMER");

        User user = new User();
        user.setId(userId);
        user.setEmail("farmer7@test.com");
        user.setRoles(java.util.Set.of(farmerRole));

        CustomUserDetails principal = new CustomUserDetails(user);
        return user(principal);
}
 
    @Test
    @DisplayName("GET /api/farmers/me returns 200 with the farmer's own profile")
    void getMyProfile_returnsProfile() throws Exception {
        FarmerResponse response = new FarmerResponse(
                9L, "farmer7@test.com", "Ravi Kumar", "234123412346", LocalDate.of(1990, 5, 15), "Vellore");
        when(farmerService.getProfile(9L)).thenReturn(response);
 
        mockMvc.perform(get("/api/farmers/me").with(farmerAuth(9L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(9))
                .andExpect(jsonPath("$.fullName").value("Ravi Kumar"));
    }
 
    @Test
    @DisplayName("POST /api/farmers/me with a blank fullName returns 400 with a field error")
    void createProfile_invalidBody_returns400() throws Exception {
        // fullName blank — should never reach the service at all.
        String invalidJson = """
                {"fullName":"","aadhaarNumber":"234123412346","address":"Vellore"}
                """;
 
        mockMvc.perform(post("/api/farmers/me")
                        .with(farmerAuth(9L))
                        .with(csrf())
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists());
    }
 
    @Test
    @DisplayName("POST /api/farmers/me with a bad Aadhaar checksum returns 400")
    void createProfile_invalidAadhaar_returns400() throws Exception {
        FarmerRequest request = new FarmerRequest("Ravi Kumar", "123456789012", null, null);
 
        mockMvc.perform(post("/api/farmers/me")
                        .with(farmerAuth(9L))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.aadhaarNumber").exists());
    }
 
    @Test
    @DisplayName("GET /api/farmers/me/dashboard delegates to DashboardService, not FarmerService")
    void getMyDashboard_delegatesToDashboardService() throws Exception {
        when(dashboardService.farmerDashboard(9L)).thenReturn(
                new com.agrofinance.dto.FarmerDashboardResponse(
                        4, java.util.Map.of(), new java.math.BigDecimal("2.5"), 1, 0));
 
        mockMvc.perform(get("/api/farmers/me/dashboard").with(farmerAuth(9L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLoans").value(4));
    }
 
}
 


































