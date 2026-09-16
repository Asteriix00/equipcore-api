package dev.asterix.equipcore_api.controller;

import dev.asterix.equipcore_api.dto.auth.LoginRequest;
import dev.asterix.equipcore_api.dto.auth.LoginResponse;
import dev.asterix.equipcore_api.security.UserPrincipal;
import dev.asterix.equipcore_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PatchMapping
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {

        authService.logout(userPrincipal);

        return ResponseEntity.noContent().build();
    }
}
