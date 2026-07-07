package com.spkmainan.toy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ToyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void list_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/toys")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void list_isPaged() throws Exception {
        mockMvc.perform(get("/toys").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements")
                .value(org.hamcrest.Matchers.greaterThanOrEqualTo(50)));
    }

    @Test
    @WithMockUser
    void create_thenFetch_withScores() throws Exception {
        var request = new ToyDto.Request("Mainan Uji", "edukatif", 50000, 3, 6, 5, true,
            "deskripsi", Set.of("Kayu"), Map.of("keamanan", 4, "edukasi", 5));
        String json = mockMvc.perform(post("/toys")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Mainan Uji"))
            .andExpect(jsonPath("$.categoryName").value("Edukatif"))
            .andExpect(jsonPath("$.scores.keamanan").value(4))
            .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(json).get("id").asLong();
        mockMvc.perform(get("/toys/" + id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void create_unknownCategory_returns400() throws Exception {
        var request = new ToyDto.Request("X", "tidakada", 1000, 1, 2, 1, true, null, null, null);
        mockMvc.perform(post("/toys")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateScores_outOfRange_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
            new ToyDto.ScoresRequest(Map.of("keamanan", 9)));
        mockMvc.perform(put("/toys/1/scores")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }
}
