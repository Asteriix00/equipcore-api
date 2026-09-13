package dev.asterix.equipcore_api.security;

import dev.asterix.equipcore_api.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtConfig jwtConfig;

    @Override
    public String generateToken(UserPrincipal userPrincipal) {

        return Jwts
                .builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(
                        Date.from(Instant.now())
                )
                .expiration(
                        Date.from(
                                Instant.now().plusSeconds(jwtConfig.getExpiration())
                        )
                )
                .claim("role", userPrincipal.getUser().getRole().name())
                .signWith(getSecretKey())
                .compact();
    }

    public String extractSubject(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String email, String token) {

        return extractSubject(token).equals(email) && !isTokenExpired(token);
    }

    private SecretKey getSecretKey() {

        byte[] byteKey = Decoders.BASE64.decode(jwtConfig.getSecretKey());
        return Keys.hmacShaKeyFor(byteKey);
    }

    private Claims extractPayload(String token) {

        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> functionResolver) {

        final Claims payload = extractPayload(token);

        return functionResolver.apply(payload);
    }

    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }
}
