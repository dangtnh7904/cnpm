package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.Role;
import com.nhom33.quanlychungcu.entity.UserAccount;
import com.nhom33.quanlychungcu.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserAccount>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAccount> updateUser(@PathVariable Integer id, @Valid @RequestBody UpdateUserRequest request) {
        UserAccount updatedUser = userService.updateUser(
                id,
                request.fullName(),
                request.email(),
                request.role(),
                request.password()
        );
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    public record UpdateUserRequest(
            @NotBlank @Size(max = 100) String fullName,
            @Email @NotBlank @Size(max = 150) String email,
            Role role,
            String password // Optional, can be null/empty
    ) {}
}
