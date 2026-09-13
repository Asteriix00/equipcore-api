package dev.asterix.equipcore_api.security;

import dev.asterix.equipcore_api.config.JwtConfig;
import dev.asterix.equipcore_api.enumeration.UserRole;
import dev.asterix.equipcore_api.model.User;
import dev.asterix.equipcore_api.support.JwtTestSupport;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceImplTest {

    @Autowired
    private JwtServiceImpl jwtService;

    @Autowired
    private JwtConfig jwtConfig;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {

        User user = User
                .builder()
                .id(UUID.randomUUID())
                .firstName("Amine")
                .lastName("CHAKHAR")
                .email("amine.chakhar@gmail.com")
                .password("$2a$12$85NwZwUWcg9ve9Ry7MCmoefueK0fNTGyD2n5W7RdmAGqbGA9YoiU2")
                .role(UserRole.ADMIN)
                .isEnabled(true)
                .build();

        userPrincipal = new UserPrincipal(user);
    }

    @Test
    void generateToken_withValidUserPrincipal() {

        String token = jwtService.generateToken(userPrincipal);

        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isBlank());

        Claims claims = JwtTestSupport.extractPayload(token, jwtConfig);

        Assertions.assertAll(
                () -> Assertions.assertEquals(userPrincipal.getUsername(), claims.getSubject()),
                () -> Assertions.assertNotNull(claims.getIssuedAt()),
                () -> Assertions.assertNotNull(claims.getExpiration()),
                () -> Assertions.assertTrue(claims.getExpiration().after(claims.getIssuedAt())),
                () -> Assertions.assertEquals(userPrincipal.getUser().getRole().name(), claims.get("role", String.class))
        );
    }

    @Test
    void generateToken_withTokenNull() {

        Assertions.assertThrows(
                NullPointerException.class,
                () -> jwtService.generateToken(null)
        );
    }

    @Test
    void extractSubject_withValidToken() {

        String token = jwtService.generateToken(userPrincipal);

        String subject = jwtService.extractSubject(token);

        Assertions.assertEquals(userPrincipal.getUsername(), subject);
    }

    @Test
    void extractSubject_withNull_ThrowException() {

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.extractSubject(null)
        );
    }

    @Test
    void isTokenValid_withValidEmailAndValidToken() {

        String token = jwtService.generateToken(userPrincipal);

        boolean isValid = jwtService.isTokenValid(userPrincipal.getUsername(), token);

        Assertions.assertTrue(isValid);
    }

    @Test
    void isTokenValid_withInvalidEmailAndValidToken() {

        String token = jwtService.generateToken(userPrincipal);

        boolean isValid = jwtService.isTokenValid("amine@email.com", token);

        Assertions.assertFalse(isValid);
    }

    @Test
    void isTokenValid_withValidEmailAndInvalidToken() {

        String email = userPrincipal.getUsername();

        Assertions.assertThrows(
                MalformedJwtException.class,
                () -> jwtService.isTokenValid(email, "invalid token value")
        );
    }

    @Test
    void isTokenValid_withValidEmailAndExpiredToken() {

        String email = userPrincipal.getUsername();
        String token = JwtTestSupport.generateExpiredToken(userPrincipal, jwtConfig);

        Assertions.assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(email, token)
        );
    }
}