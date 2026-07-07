package com.spkmainan.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void summary_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/dashboard/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void summary_returnsCountsAndTop5() throws Exception {
        mockMvc.perform(get("/dashboard/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCriteria").value(10))
            .andExpect(jsonPath("$.totalProfiles").value(5))
            .andExpect(jsonPath("$.totalToys").value(Matchers.greaterThanOrEqualTo(50)))
            .andExpect(jsonPath("$.categoryDistribution.length()").value(Matchers.greaterThanOrEqualTo(8)))
            .andExpect(jsonPath("$.top5.length()").value(5))
            .andExpect(jsonPath("$.recentSessions").isArray());
    }
}
