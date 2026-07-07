package com.spkmainan.category;

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

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void list_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/categories")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void list_returnsSeededCategories() throws Exception {
        mockMvc.perform(get("/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(8))
            .andExpect(jsonPath("$[0].toyCount").isNumber());
    }

    @Test
    @WithMockUser
    void create_thenDelete_emptyCategory() throws Exception {
        String body = objectMapper.writeValueAsString(
            new CategoryDto.Request("Kategori Uji Baru", "deskripsi uji"));
        String json = mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("kategori-uji-baru"))
            .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(json).get("id").asLong();
        mockMvc.perform(delete("/categories/" + id)).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void delete_categoryWithToys_returns409() throws Exception {
        // Find a seeded category that has toys.
        String json = mockMvc.perform(get("/categories")).andReturn().getResponse().getContentAsString();
        long idWithToys = 0;
        for (JsonNode c : objectMapper.readTree(json)) {
            if (c.get("toyCount").asLong() > 0) {
                idWithToys = c.get("id").asLong();
                break;
            }
        }
        mockMvc.perform(delete("/categories/" + idWithToys)).andExpect(status().isConflict());
    }
}
