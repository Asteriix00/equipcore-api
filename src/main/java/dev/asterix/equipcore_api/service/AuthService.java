package dev.asterix.equipcore_api.service;

import dev.asterix.equipcore_api.dto.auth.LoginRequest;
import dev.asterix.equipcore_api.dto.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);
}
