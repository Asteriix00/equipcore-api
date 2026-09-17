package dev.asterix.equipcore_api.service.implementation;

import dev.asterix.equipcore_api.config.JwtConfig;
import dev.asterix.equipcore_api.dto.auth.LoginRequest;
import dev.asterix.equipcore_api.dto.auth.LoginResponse;
import dev.asterix.equipcore_api.enumeration.UserRole;
import dev.asterix.equipcore_api.model.User;
import dev.asterix.equipcore_api.repository.UserRepository;
import dev.asterix.equipcore_api.security.UserPrincipal;
import dev.asterix.equipcore_api.service.AuthService;
import dev.asterix.equipcore_api.support.JwtTestSupport;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceImplTest {

    private static final String RAW_PASSWORD = "Password@123";

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private UserPrincipal userPrincipal;

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

        userPrincipal = new UserPrincipal(user);
    }

    @Test
    void login_withValidCredentials() {

        LoginRequest loginRequest = new LoginRequest(user.getEmail(), RAW_PASSWORD);
        LoginResponse loginResponse = authService.login(loginRequest);

        Assertions.assertNotNull(loginResponse.token());
        Assertions.assertFalse(loginResponse.token().isBlank());

        Claims claims = JwtTestSupport.extractPayload(loginResponse.token(), jwtConfig);

        Assertions.assertEquals(user.getEmail(), claims.getSubject());
        Assertions.assertEquals(user.getRole().name(), claims.get("role", String.class));
    }

    @Test
    void login_withWrongPassword() {

        LoginRequest loginRequest = new LoginRequest(user.getEmail(), "Password@1234");

        Assertions.assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
    }

    @Test
    void login_withWrongEmail() {

        LoginRequest loginRequest = new LoginRequest("amine@gmail.com", RAW_PASSWORD);

        Assertions.assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
    }

    @Test
    void login_withDisabledAccount() {

        user.setEnabled(false);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest(user.getEmail(), RAW_PASSWORD);

        Assertions.assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
    }

    @Test
    void logout_withValidUserPrincipal() {

        int initialTokenVersion = user.getTokenVersion();

        authService.logout(userPrincipal);

        User currentUser = userRepository.findByEmail(user.getEmail()).orElseThrow();

        Assertions.assertEquals(initialTokenVersion + 1, currentUser.getTokenVersion());
    }

    @Test
    void logout_withUserPrincipalNull() {

        Assertions.assertThrows(
                NullPointerException.class,
                () -> authService.logout(null)
        );
    }
}