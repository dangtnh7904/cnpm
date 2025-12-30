package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.Role;
import com.nhom33.quanlychungcu.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        String token = authService.signup(
                request.username(),
                request.password(),
                request.fullName(),
                request.email(),
                request.role()
        );
        return ResponseEntity.ok(new AuthResponse(request.username(), request.role(), token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.loginWithRole(request.username(), request.password());
        return ResponseEntity.ok(new AuthResponse(result.username(), result.role(), result.token()));
    }

    // ===== DTOs =====
    public record SignupRequest(
            @NotBlank @Size(min = 4, max = 100) String username,
            @NotBlank @Size(min = 6, max = 100) String password,
            @NotBlank @Size(max = 100) String fullName,
            @Email @NotBlank @Size(max = 150) String email,
            Role role
    ) {}

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record AuthResponse(String username, Role role, String token) {}
}
