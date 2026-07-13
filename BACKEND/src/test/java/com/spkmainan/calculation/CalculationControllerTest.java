package com.spkmainan.calculation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spkmainan.weightprofile.WeightProfileDto;
import java.util.List;
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
@Transactional
class CalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CalculationRunRepository runRepo;

    @Test
    @WithMockUser
    void run_freezesCriteriaWeightsAndNorm() throws Exception {
        String body = mockMvc.perform(post("/calculations/run"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(body).get("id").asLong();

        CalculationRun run = runRepo.findById(id).orElseThrow();
        // 10 active criteria seeded → 10 frozen criteria + per-profile 10 weights; norm non-empty.
        assertThat(run.getCriteria()).hasSize(10);
        assertThat(run.getNorms()).isNotEmpty();
        assertThat(run.getResults().get(0).getWeights()).hasSize(10);
        assertThat(run.getResults().get(0).getShortName()).isNotBlank();
    }

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
    void run_rejectedWhenAProfileIsInconsistent() throws Exception {
        // Make profile 1 inconsistent: a maximal-intensity 3-cycle (A>B>C>A at 9) → CR ≫ 0.10.
        var entries = List.of(
            new WeightProfileDto.PairwiseEntry("keamanan", "edukasi", 9.0),
            new WeightProfileDto.PairwiseEntry("edukasi", "usia", 9.0),
            new WeightProfileDto.PairwiseEntry("usia", "keamanan", 9.0));
        String body = objectMapper.writeValueAsString(new WeightProfileDto.PairwiseRequest(entries));
        mockMvc.perform(put("/weight-profiles/1/pairwise")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());

        // precheck now fails (a profile has CR > 0.10) → run must be rejected.
        mockMvc.perform(post("/calculations/run")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void publish_thenUnpublish_marksSessionUnpublished() throws Exception {
        String runJson = mockMvc.perform(post("/calculations/run"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(runJson).get("id").asLong();

        mockMvc.perform(post("/calculations/" + id + "/publish"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.published").value(true));

        mockMvc.perform(post("/calculations/" + id + "/unpublish"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.published").value(false));
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
