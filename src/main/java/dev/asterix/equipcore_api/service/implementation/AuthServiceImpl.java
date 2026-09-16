package dev.asterix.equipcore_api.service.implementation;

import dev.asterix.equipcore_api.dto.auth.LoginRequest;
import dev.asterix.equipcore_api.dto.auth.LoginResponse;
import dev.asterix.equipcore_api.security.JwtService;
import dev.asterix.equipcore_api.security.UserPrincipal;
import dev.asterix.equipcore_api.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(userPrincipal);

        return new LoginResponse(token);
    }
}
