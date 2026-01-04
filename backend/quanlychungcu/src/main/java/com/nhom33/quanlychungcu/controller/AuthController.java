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
    private final com.nhom33.quanlychungcu.repository.UserAccountRepository userRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService,
                          com.nhom33.quanlychungcu.repository.UserAccountRepository userRepo,
                          org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Reset password cho user có sẵn trong database (dùng để fix user seed)
     * POST /api/auth/reset-seed-users
     */
    @PostMapping("/reset-seed-users")
    public ResponseEntity<?> resetSeedUsers() {
        java.util.List<String[]> seedUsers = java.util.List.of(
            new String[]{"admin", "Admin@123", "ADMIN"},
            new String[]{"manager", "Manager@123", "MANAGER"},
            new String[]{"manager2", "Manager@123", "MANAGER"},
            new String[]{"accountant", "Accountant@123", "ACCOUNTANT"},
            new String[]{"resident", "Resident@123", "RESIDENT"}
        );
        
        java.util.List<String> results = new java.util.ArrayList<>();
        
        for (String[] userData : seedUsers) {
            String username = userData[0];
            String password = userData[1];
            String roleStr = userData[2];
            
            var userOpt = userRepo.findByUsername(username);
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                user.setPassword(passwordEncoder.encode(password));
                userRepo.save(user);
                results.add(username + ": password reset OK");
            } else {
                // Tạo user mới
                var user = new com.nhom33.quanlychungcu.entity.UserAccount();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setFullName(username);
                user.setEmail(username + "@example.com");
                user.setRole(com.nhom33.quanlychungcu.entity.Role.valueOf(roleStr));
                userRepo.save(user);
                results.add(username + ": created new");
            }
        }
        
        return ResponseEntity.ok(java.util.Map.of("results", results));
    }

    /**
     * Debug endpoint - kiểm tra thông tin user hiện tại
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(java.util.Map.of(
                "authenticated", false,
                "message", "No authentication found"
            ));
        }
        return ResponseEntity.ok(java.util.Map.of(
            "authenticated", true,
            "username", authentication.getName(),
            "authorities", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList(),
            "principal", authentication.getPrincipal().getClass().getSimpleName()
        ));
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
