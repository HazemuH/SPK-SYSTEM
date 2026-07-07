package com.spkmainan.publicapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The public API must work WITHOUT authentication (mobile is login-less) and
 * return the AHP-SAW-backed shapes the app expects.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void meta_isPublic_andReturnsSeededReferenceData() throws Exception {
        mockMvc.perform(get("/public/meta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categories.length()").value(8))
            .andExpect(jsonPath("$.criteria.length()").value(10))
            .andExpect(jsonPath("$.profiles.length()").value(5));
    }

    @Test
    void top_returnsRankedList() throws Exception {
        mockMvc.perform(get("/public/top").param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(5))
            .andExpect(jsonPath("$[0].rank").value(1))
            .andExpect(jsonPath("$[0].toy.name").isNotEmpty())
            .andExpect(jsonPath("$[0].score").isNumber());
    }

    @Test
    void catalog_sortByCheapest_ranksLowestPriceFirst() throws Exception {
        mockMvc.perform(get("/public/toys").param("sort", "harga"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    void toyDetail_hasRanksAndNormalizedScores() throws Exception {
        mockMvc.perform(get("/public/toys/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.globalRank").isNumber())
            .andExpect(jsonPath("$.normalized").isMap())
            .andExpect(jsonPath("$.strengths.length()").value(2));
    }

    @Test
    void toyDetail_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/public/toys/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void recommend_returnsProfileAndLists() throws Exception {
        String body = """
            {"usia":"3-5","budget":"300000","tujuan":"edukatif","prioritas":"edukasi"}
            """;
        mockMvc.perform(post("/public/recommend")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileId").value("education"))
            .andExpect(jsonPath("$.primary").isArray())
            .andExpect(jsonPath("$.others").isArray());
    }

    @Test
    void compare_returnsRowsAndTotals() throws Exception {
        mockMvc.perform(get("/public/compare").param("ids", "1,2,3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.toys.length()").value(3))
            .andExpect(jsonPath("$.rows.length()").value(10))
            .andExpect(jsonPath("$.totals.length()").value(3));
    }
}
