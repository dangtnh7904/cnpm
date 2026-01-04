package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.UserToaNha;
import com.nhom33.quanlychungcu.service.UserToaNhaService;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý liên kết User với Tòa nhà.
 * 
 * API cho Manager gắn user vào tòa nhà mình quản lý.
 */
@RestController
@RequestMapping("/api/user-toa-nha")
public class UserToaNhaController {

    private final UserToaNhaService service;

    public UserToaNhaController(UserToaNhaService service) {
        this.service = service;
    }

    /**
     * Gắn user vào tòa nhà (bằng username).
     * POST /api/user-toa-nha
     * Body: { "username": "user1", "toaNhaId": 1 }
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<UserToaNha> addUserToBuilding(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        Integer toaNhaId = (Integer) request.get("toaNhaId");
        
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        if (toaNhaId == null) {
            throw new IllegalArgumentException("Phải chọn tòa nhà");
        }
        
        UserToaNha result = service.addUserToBuilding(username, toaNhaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Xóa user khỏi tòa nhà.
     * DELETE /api/user-toa-nha/{userId}/{toaNhaId}
     */
    @DeleteMapping("/{userId}/{toaNhaId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Map<String, String>> removeUserFromBuilding(
            @PathVariable @NonNull Integer userId,
            @PathVariable @NonNull Integer toaNhaId) {
        
        service.removeUserFromBuilding(userId, toaNhaId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã xóa user khỏi tòa nhà");
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách user trong tòa nhà.
     * GET /api/user-toa-nha/toa-nha/{toaNhaId}
     */
    @GetMapping("/toa-nha/{toaNhaId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<UserToaNha>> getUsersInBuilding(@PathVariable @NonNull Integer toaNhaId) {
        List<UserToaNha> result = service.getUsersInBuilding(toaNhaId);
        return ResponseEntity.ok(result);
    }

    /**
     * Tìm user trong tòa nhà theo username.
     * GET /api/user-toa-nha/toa-nha/{toaNhaId}/search?username=abc
     */
    @GetMapping("/toa-nha/{toaNhaId}/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<UserToaNha>> searchUsersInBuilding(
            @PathVariable @NonNull Integer toaNhaId,
            @RequestParam(required = false, defaultValue = "") String username) {
        
        List<UserToaNha> result = service.searchUsersInBuilding(toaNhaId, username);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy danh sách tòa nhà mà user được gắn vào.
     * GET /api/user-toa-nha/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RESIDENT')")
    public ResponseEntity<List<UserToaNha>> getBuildingsOfUser(@PathVariable @NonNull Integer userId) {
        List<UserToaNha> result = service.getBuildingsOfUser(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy danh sách tòa nhà của user hiện tại (cho RESIDENT).
     * GET /api/user-toa-nha/my-buildings
     */
    @GetMapping("/my-buildings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserToaNha>> getMyBuildings() {
        // Lấy user hiện tại từ SecurityContext
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.ok(List.of());
        }
        
        // Giả sử principal là UserAccount
        if (authentication.getPrincipal() instanceof com.nhom33.quanlychungcu.entity.UserAccount user) {
            List<UserToaNha> result = service.getBuildingsOfUser(user.getId());
            return ResponseEntity.ok(result);
        }
        
        return ResponseEntity.ok(List.of());
    }

    /**
     * User tự thoát khỏi tòa nhà.
     * DELETE /api/user-toa-nha/leave/{toaNhaId}
     */
    @DeleteMapping("/leave/{toaNhaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> leaveBuilding(@PathVariable Integer toaNhaId) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof com.nhom33.quanlychungcu.entity.UserAccount user)) {
            throw new IllegalStateException("Không xác định được user hiện tại");
        }
        
        service.leaveBuilding(user.getId(), toaNhaId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã thoát khỏi tòa nhà thành công");
        return ResponseEntity.ok(response);
    }
}
