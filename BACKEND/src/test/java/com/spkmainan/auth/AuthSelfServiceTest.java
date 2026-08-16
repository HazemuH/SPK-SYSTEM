package com.spkmainan.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spkmainan.auth.dto.ChangePasswordRequest;
import com.spkmainan.auth.dto.LoginRequest;
import com.spkmainan.auth.dto.UpdateProfileRequest;
import com.spkmainan.user.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Self-service profile update and password change for the current user. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthSelfServiceTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    private String login(String username, String password) throws Exception {
        String body = om.writeValueAsString(new LoginRequest(username, password));
        String res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readTree(res).get("token").asText();
    }

    /** Create a throwaway USER via the admin API and return their username. */
    private String seedUser(String password) throws Exception {
        String adminToken = login("admin", "password123");
        String username = "self" + System.nanoTime();
        String body = om.writeValueAsString(
                new CreateUserRequest(username, username + "@kidora.test", "Self User", password, "USER"));
        mockMvc.perform(post("/users").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        return username;
    }

    @Test
    void updateProfile_changesNameAndEmail() throws Exception {
        String username = seedUser("password123");
        String token = login(username, "password123");
        String body = om.writeValueAsString(
                new UpdateProfileRequest("Nama Baru", username + "-new@kidora.test", null));
        mockMvc.perform(put("/auth/profile").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nama Baru"))
                .andExpect(jsonPath("$.email").value(username + "-new@kidora.test"));
    }

    @Test
    void changePassword_thenOldFailsAndNewWorks() throws Exception {
        String username = seedUser("password123");
        String token = login(username, "password123");
        String body = om.writeValueAsString(new ChangePasswordRequest("password123", "newpass456"));
        mockMvc.perform(post("/auth/change-password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());

        // old password rejected
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new LoginRequest(username, "password123"))))
                .andExpect(status().isUnauthorized());
        // new password works
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new LoginRequest(username, "newpass456"))))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_wrongCurrent_returns400() throws Exception {
        String username = seedUser("password123");
        String token = login(username, "password123");
        String body = om.writeValueAsString(new ChangePasswordRequest("salahsekali", "newpass456"));
        mockMvc.perform(post("/auth/change-password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
