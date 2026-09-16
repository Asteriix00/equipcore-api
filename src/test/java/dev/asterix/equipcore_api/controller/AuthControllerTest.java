package dev.asterix.equipcore_api.controller;

import dev.asterix.equipcore_api.config.JwtConfig;
import dev.asterix.equipcore_api.dto.auth.LoginRequest;
import dev.asterix.equipcore_api.enumeration.UserRole;
import dev.asterix.equipcore_api.model.User;
import dev.asterix.equipcore_api.repository.UserRepository;
import dev.asterix.equipcore_api.support.JwtTestSupport;
import io.jsonwebtoken.Claims;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtConfig jwtConfig;

    private User user;

    @BeforeEach
    void setUp() {

        user = User
                .builder()
                .firstName("Amine")
                .lastName("CHAKHAR")
                .email("amine.chakhar@gmail.com")
                .password("$2a$12$rBaWWGyuqP3pHj/z2TSsHe4YWNRr6gGuGkUKuocgzwcSfKcOZk0YO")
                .role(UserRole.ADMIN)
                .isEnabled(true)
                .build();

        userRepository.save(user);
    }

    @Test
    void login_withValidCredentials() throws Exception {

        LoginRequest loginRequest = new LoginRequest("amine.chakhar@gmail.com", "Password@123");

        MvcResult result = mockMvc.perform(
                        post("/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asString();

        Assertions.assertFalse(token.isBlank());

        Claims claims = JwtTestSupport.extractPayload(token, jwtConfig);

        Assertions.assertEquals(user.getEmail(), claims.getSubject());
        Assertions.assertEquals(user.getRole().name(), claims.get("role", String.class));
    }

    @Test
    void login_withIncorrectPassword() throws Exception {

        LoginRequest loginRequest = new LoginRequest(user.getEmail(), "Password@1234");

        mockMvc.perform(
                        post("/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email or password is incorrect"))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    void login_withIncorrectEmail() throws Exception {

        LoginRequest loginRequest = new LoginRequest("amine@gmail.com", "Password@123");

        mockMvc.perform(
                        post("/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email or password is incorrect"))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));

    }

    @Test
    void login_withMalformedRequest() throws Exception {

        mockMvc.perform(
                        post("/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ \"email\": \"amine.chakhar@gmail.com\", \"password\": }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request is malformed"))
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_REQUEST"));
    }

    @Test
    void login_withBlankEmailAndPassword() throws Exception {

        LoginRequest loginRequest = new LoginRequest("", "");

        mockMvc.perform(
                        post("/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errorCodes", Matchers.containsInAnyOrder("EMAIL_REQUIRED", "PASSWORD_REQUIRED")));
    }

    @Test
    void login_withNullEmailAndNullPassword() throws Exception {

        LoginRequest loginRequest = new LoginRequest(null, null);

        mockMvc.perform(
                        post("/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errorCodes", Matchers.containsInAnyOrder("EMAIL_REQUIRED", "PASSWORD_REQUIRED")));
    }

    @Test
    void login_withInvalidEmailFormat() throws Exception {

        LoginRequest loginRequest = new LoginRequest("amine", "Password@123");

        mockMvc.perform(
                        post("/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errorCodes").value("EMAIL_INVALID"));
    }
}