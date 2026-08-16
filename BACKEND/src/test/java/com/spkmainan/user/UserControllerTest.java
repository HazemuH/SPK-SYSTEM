package com.spkmainan.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spkmainan.auth.dto.LoginRequest;
import com.spkmainan.user.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end admin user &amp; role management against the seeded admin. */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    private String login(String username, String password) throws Exception {
        String body = om.writeValueAsString(new LoginRequest(username, password));
        String res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readTree(res).get("token").asText();
    }

    private String adminToken() throws Exception {
        return login("admin", "password123");
    }

    private String uniq() {
        return "u" + System.nanoTime();
    }

    @Test
    void list_asAdmin_returnsUsers() throws Exception {
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").isNotEmpty());
    }

    @Test
    void create_asAdmin_thenAppearsInList() throws Exception {
        String token = adminToken();
        String username = uniq();
        String body = om.writeValueAsString(
                new CreateUserRequest(username, username + "@kidora.test", "Staf Uji", "password123", "USER"));

        mockMvc.perform(post("/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Staf Uji"))
                .andExpect(jsonPath("$.role").value("user"));
    }

    @Test
    void create_duplicateUsername_returns409() throws Exception {
        String token = adminToken();
        String username = uniq();
        String body = om.writeValueAsString(
                new CreateUserRequest(username, username + "@kidora.test", "Dup", "password123", "USER"));
        mockMvc.perform(post("/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        // second create with same username → conflict
        String body2 = om.writeValueAsString(
                new CreateUserRequest(username, uniq() + "@kidora.test", "Dup2", "password123", "USER"));
        mockMvc.perform(post("/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body2))
                .andExpect(status().isConflict());
    }

    @Test
    void create_shortPassword_returns400() throws Exception {
        String username = uniq();
        String body = om.writeValueAsString(
                new CreateUserRequest(username, username + "@kidora.test", "Short", "123", "USER"));
        mockMvc.perform(post("/users").header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdmin_cannotAccessUsers_returns403() throws Exception {
        String adminToken = adminToken();
        String username = uniq();
        // admin creates a plain USER
        String body = om.writeValueAsString(
                new CreateUserRequest(username, username + "@kidora.test", "Biasa", "password123", "USER"));
        mockMvc.perform(post("/users").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        // that USER logs in and is forbidden from user management
        String userToken = login(username, "password123");
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSelf_returns409() throws Exception {
        String token = adminToken();
        String profile = mockMvc.perform(get("/auth/profile").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        String adminId = om.readTree(profile).get("id").asText();
        mockMvc.perform(delete("/users/" + adminId).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }
}
