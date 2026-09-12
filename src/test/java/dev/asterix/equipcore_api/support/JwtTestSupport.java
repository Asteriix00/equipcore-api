package dev.asterix.equipcore_api.support;

import dev.asterix.equipcore_api.config.JwtConfig;
import dev.asterix.equipcore_api.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public final class JwtTestSupport {

    private JwtTestSupport() {

        throw new UnsupportedOperationException("This is a support class for JWT");
    }

    // Generate an expired token
    public static String generateExpiredToken(UserPrincipal userPrincipal, JwtConfig jwtConfig) {

        return Jwts
                .builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .claim("role", userPrincipal.getUser().getRole().name())
                .signWith(getSecretKey(jwtConfig))
                .compact();
    }

    // Extract the token payload
    public static Claims extractPayload(String token, JwtConfig jwtConfig) {

        return Jwts
                .parser()
                .verifyWith(getSecretKey(jwtConfig))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Helper to get the secret key
    private static SecretKey getSecretKey(JwtConfig jwtConfig) {

        byte[] byteKey = Decoders.BASE64.decode(jwtConfig.getSecretKey());
        return Keys.hmacShaKeyFor(byteKey);
    }
}
