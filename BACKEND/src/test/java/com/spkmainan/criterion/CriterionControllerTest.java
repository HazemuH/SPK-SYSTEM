package com.spkmainan.criterion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class CriterionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void list_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/criteria")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void create_addsBenefitCriterion() throws Exception {
        String body = objectMapper.writeValueAsString(
            new CriterionDto.CreateRequest("Ramah Lingkungan", "benefit", "eco-friendly", "Eco"));
        String json = mockMvc.perform(post("/criteria")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("ramah-lingkungan"))
            .andExpect(jsonPath("$.type").value("benefit"))
            .andExpect(jsonPath("$.no").value(11))
            .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(json).get("id").asLong();
        mockMvc.perform(delete("/criteria/" + id)).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void delete_hargaCriterion_returns409() throws Exception {
        String json = mockMvc.perform(get("/criteria")).andReturn().getResponse().getContentAsString();
        long hargaId = 0;
        for (JsonNode c : objectMapper.readTree(json)) {
            if ("harga".equals(c.get("code").asText())) {
                hargaId = c.get("id").asLong();
                break;
            }
        }
        mockMvc.perform(delete("/criteria/" + hargaId)).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void create_invalidType_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
            new CriterionDto.CreateRequest("Aneh", "salah", null, null));
        mockMvc.perform(post("/criteria")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }
}
