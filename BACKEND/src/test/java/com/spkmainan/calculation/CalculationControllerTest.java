package com.spkmainan.calculation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void precheck_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/calculations/precheck")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void precheck_passesForSeededData() throws Exception {
        mockMvc.perform(post("/calculations/precheck"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allOk").value(true))
            .andExpect(jsonPath("$.items.length()").value(4));
    }

    @Test
    @WithMockUser
    void list_hasAtLeastTheSeededSession() throws Exception {
        mockMvc.perform(get("/calculations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @WithMockUser
    void run_thenPublish_thenDetail() throws Exception {
        // run → 5 profile results
        String runJson = mockMvc.perform(post("/calculations/run"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.results.length()").value(5))
            .andExpect(jsonPath("$.results[0].bestToyName").isNotEmpty())
            .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(runJson).get("id").asLong();

        // publish
        mockMvc.perform(post("/calculations/" + id + "/publish"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.published").value(true));

        // detail → per-profile rankings present
        mockMvc.perform(get("/calculations/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results.length()").value(5))
            .andExpect(jsonPath("$.results[0].ranking.length()").value(Matchers.greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.results[0].ranking[0].rank").value(1));
    }
}
