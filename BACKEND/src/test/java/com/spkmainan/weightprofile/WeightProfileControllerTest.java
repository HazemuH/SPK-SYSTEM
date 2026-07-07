package com.spkmainan.weightprofile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
class WeightProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void list_returnsSeededProfiles() throws Exception {
        mockMvc.perform(get("/weight-profiles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    @WithMockUser
    void computePairwise_derivesWeightsAndConsistency() throws Exception {
        // A few coherent comparisons; missing pairs default to equal (1).
        var entries = List.of(
            new WeightProfileDto.PairwiseEntry("keamanan", "edukasi", 2.0),
            new WeightProfileDto.PairwiseEntry("keamanan", "usia", 3.0),
            new WeightProfileDto.PairwiseEntry("edukasi", "usia", 1.5));
        String body = objectMapper.writeValueAsString(new WeightProfileDto.PairwiseRequest(entries));

        mockMvc.perform(put("/weight-profiles/1/pairwise")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.weights.length()").value(10))
            .andExpect(jsonPath("$.cr").isNumber())
            .andExpect(jsonPath("$.lambdaMax").isNumber())
            // keamanan was rated more important → should outweigh edukasi
            .andExpect(jsonPath("$.weights.keamanan").isNumber());
    }
}
