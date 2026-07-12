package com.spkmainan.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    @WithMockUser
    void publishStatus_isNotStale_forFreshlySeededAndPublishedData() throws Exception {
        // Seeder runs + publishes at boot; nothing edited since → published & not stale.
        mockMvc.perform(get("/dashboard/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publishStatus.published").value(true))
            .andExpect(jsonPath("$.publishStatus.lastPublishedAt").isNotEmpty())
            .andExpect(jsonPath("$.publishStatus.stale").value(false));
    }

    @Test
    @WithMockUser
    @Transactional
    void publishStatus_becomesStale_afterAnAdminEdit() throws Exception {
        // Edit a toy's ratings after publish → the published snapshot is now stale.
        String scores = "{\"scores\":{\"keamanan\":2,\"edukasi\":2,\"usia\":2,\"kualitas\":2,"
            + "\"tahan\":2,\"material\":2,\"kreatif\":2,\"populer\":2,\"mudah\":2}}";
        mockMvc.perform(put("/toys/1/scores")
                .contentType(MediaType.APPLICATION_JSON).content(scores))
            .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publishStatus.stale").value(true));
    }
}
