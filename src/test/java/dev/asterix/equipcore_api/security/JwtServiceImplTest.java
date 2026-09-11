package dev.asterix.equipcore_api.security;

import dev.asterix.equipcore_api.config.JwtConfig;
import dev.asterix.equipcore_api.enumeration.UserRole;
import dev.asterix.equipcore_api.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.util.Date;
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
    public void setUp() {

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

        Claims claims = extractPayload(token);

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

        Boolean isValid = jwtService.isTokenValid(userPrincipal.getUsername(), token);

        Assertions.assertTrue(isValid);
    }

    @Test
    void isTokenValid_withInvalidEmailAndValidToken() {

        String token = jwtService.generateToken(userPrincipal);

        Boolean isValid = jwtService.isTokenValid("amine@email.com", token);

        Assertions.assertFalse(isValid);
    }

    @Test
    void isTokenValid_withValidEmailAndInvalidToken() {

        Assertions.assertThrows(
                MalformedJwtException.class,
                () -> jwtService.isTokenValid(userPrincipal.getUsername(), "invalid token value")
        );
    }

    @Test
    void isTokenValid_withValidEmailAndExpiredToken() {

        String token = generateExpiredToken();

        Assertions.assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(userPrincipal.getUsername(), token)
        );
    }

    // Helper to get the secret key
    private SecretKey getSecretKey() {

        byte[] byteKey = Decoders.BASE64.decode(jwtConfig.getSecretKey());
        return Keys.hmacShaKeyFor(byteKey);
    }

    // Helper to extract the token payload
    private Claims extractPayload(String token) {

        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Helper to generate an expired token
    private String generateExpiredToken() {

        return Jwts
                .builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .claim("role", userPrincipal.getUser().getRole().name())
                .signWith(getSecretKey())
                .compact();
    }
}