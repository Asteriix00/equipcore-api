package dev.asterix.equipcore_api.controller;

import dev.asterix.equipcore_api.dto.auth.LoginRequest;
import dev.asterix.equipcore_api.dto.auth.LoginResponse;
import dev.asterix.equipcore_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
