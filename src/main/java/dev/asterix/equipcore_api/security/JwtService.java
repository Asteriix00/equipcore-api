package dev.asterix.equipcore_api.security;

public interface JwtService {

    String generateToken(UserPrincipal userPrincipal);

    String extractSubject(String token);

    boolean isTokenValid(String email, String token);
}
