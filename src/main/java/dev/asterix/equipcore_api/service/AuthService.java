package dev.asterix.equipcore_api.service;

import dev.asterix.equipcore_api.dto.auth.LoginRequest;
import dev.asterix.equipcore_api.dto.auth.LoginResponse;
import dev.asterix.equipcore_api.security.UserPrincipal;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    void logout(UserPrincipal userPrincipal);
}
