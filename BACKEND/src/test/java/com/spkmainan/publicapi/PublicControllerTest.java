package com.spkmainan.publicapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * The public API must work WITHOUT authentication (mobile is login-less) and
 * return the AHP-SAW-backed shapes the app expects — served from the latest
 * PUBLISHED snapshot (the publish gate).
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
    void recommend_acceptsProfileCodeDirectly() throws Exception {
        // Mobile now sends the chosen weight-profile code as `prioritas` (1:1 with AHP).
        String body = """
            {"usia":"3-5","budget":"300000","tujuan":"edukatif","prioritas":"safety"}
            """;
        mockMvc.perform(post("/public/recommend")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileId").value("safety"));
    }

    @Test
    void compare_returnsRowsAndTotals() throws Exception {
        mockMvc.perform(get("/public/compare").param("ids", "1,2,3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.toys.length()").value(3))
            .andExpect(jsonPath("$.rows.length()").value(10))
            .andExpect(jsonPath("$.totals.length()").value(3));
    }

    /** THE GATE: an admin rating change is NOT visible on mobile until re-run + re-publish. */
    @Test
    @Transactional
    @WithMockUser
    void publicTop_ignoresAdminEdits_untilRepublished() throws Exception {
        String before = mockMvc.perform(get("/public/top").param("limit", "10"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Admin slams toy 1's benefit ratings to the minimum — this would tank its live SAW score.
        String scores = "{\"scores\":{\"keamanan\":1,\"edukasi\":1,\"usia\":1,\"kualitas\":1,"
            + "\"tahan\":1,\"material\":1,\"kreatif\":1,\"populer\":1,\"mudah\":1}}";
        mockMvc.perform(put("/toys/1/scores")
                .contentType(MediaType.APPLICATION_JSON).content(scores))
            .andExpect(status().isOk());

        String after = mockMvc.perform(get("/public/top").param("limit", "10"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Frozen: the published ranking/scores are byte-identical despite the admin edit.
        assertThat(after).isEqualTo(before);
    }

    /** A toy added after publish is hidden from the public API until re-run + re-publish. */
    @Test
    @Transactional
    @WithMockUser
    void newToyAfterPublish_isHiddenUntilRepublished() throws Exception {
        String newToy = "{\"name\":\"ZZZ Uji Gerbang\",\"categoryCode\":\"edukatif\",\"price\":50000,"
            + "\"ageMin\":3,\"ageMax\":8,\"stock\":5,\"active\":true,"
            + "\"scores\":{\"keamanan\":5,\"edukasi\":5,\"usia\":5,\"kualitas\":5,\"tahan\":5,"
            + "\"material\":5,\"kreatif\":5,\"populer\":5,\"mudah\":5}}";
        mockMvc.perform(post("/toys").contentType(MediaType.APPLICATION_JSON).content(newToy))
            .andExpect(status().isCreated());

        // Not in the published snapshot → absent from the public catalog.
        mockMvc.perform(get("/public/toys").param("search", "ZZZ Uji Gerbang"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }
}
