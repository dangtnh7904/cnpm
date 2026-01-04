package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.Role;
import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.entity.UserAccount;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;
import com.nhom33.quanlychungcu.repository.UserAccountRepository;
import com.nhom33.quanlychungcu.repository.UserToaNhaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class để xử lý logic phân quyền Multi-Tenancy.
 * 
 * Logic phân quyền:
 * - ADMIN: Xem/quản lý tất cả dữ liệu
 * - MANAGER: Chỉ xem/quản lý dữ liệu của tòa nhà mình quản lý
 * - ACCOUNTANT: Xem tất cả (để làm báo cáo), nhưng không quản lý tòa nhà
 * - RESIDENT: Xem dữ liệu của tòa nhà được gắn vào (qua UserToaNha)
 */
@Component
public class SecurityHelper {

    private final UserAccountRepository userRepo;
    private final ToaNhaRepository toaNhaRepo;
    private final UserToaNhaRepository userToaNhaRepo;

    public SecurityHelper(UserAccountRepository userRepo, 
                          ToaNhaRepository toaNhaRepo,
                          UserToaNhaRepository userToaNhaRepo) {
        this.userRepo = userRepo;
        this.toaNhaRepo = toaNhaRepo;
        this.userToaNhaRepo = userToaNhaRepo;
    }

    /**
     * Lấy user đang đăng nhập
     */
    public UserAccount getCurrentUser() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepo.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lấy ID của user đang đăng nhập
     */
    public Integer getCurrentUserId() {
        UserAccount user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Kiểm tra user có phải Admin hệ thống không
     */
    public boolean isSystemAdmin() {
        UserAccount user = getCurrentUser();
        return user != null && user.getRole() == Role.ADMIN;
    }

    /**
     * Kiểm tra user có phải Manager không
     */
    public boolean isManager() {
        UserAccount user = getCurrentUser();
        return user != null && user.getRole() == Role.MANAGER;
    }

    /**
     * Kiểm tra user có phải Accountant không
     */
    public boolean isAccountant() {
        UserAccount user = getCurrentUser();
        return user != null && user.getRole() == Role.ACCOUNTANT;
    }

    /**
     * Kiểm tra user có phải Resident không
     */
    public boolean isResident() {
        UserAccount user = getCurrentUser();
        return user != null && user.getRole() == Role.RESIDENT;
    }

    /**
     * Kiểm tra user có quyền xem tất cả dữ liệu không
     * - ADMIN: Quản trị hệ thống, xem tất cả
     * - ACCOUNTANT: Kế toán, cần xem tất cả để làm báo cáo
     */
    public boolean canViewAll() {
        UserAccount user = getCurrentUser();
        return user != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.ACCOUNTANT);
    }

    /**
     * Lấy danh sách ID tòa nhà mà user hiện tại được phép truy cập
     * - ADMIN: Tất cả tòa nhà
     * - MANAGER: Tòa nhà mình sở hữu/quản lý
     * - ACCOUNTANT/RESIDENT: Tòa nhà được gắn vào (qua UserToaNha)
     */
    public List<Integer> getAccessibleBuildingIds() {
        UserAccount user = getCurrentUser();
        
        if (user == null) {
            return List.of();
        }

        // Admin xem tất cả
        if (user.getRole() == Role.ADMIN) {
            return toaNhaRepo.findAll().stream()
                .map(ToaNha::getId)
                .collect(Collectors.toList());
        }

        // Manager xem tòa nhà mình sở hữu/quản lý
        if (user.getRole() == Role.MANAGER) {
            return toaNhaRepo.findByNguoiQuanLyId(user.getId()).stream()
                .map(ToaNha::getId)
                .collect(Collectors.toList());
        }

        // ACCOUNTANT/RESIDENT xem tòa nhà được gắn vào (qua UserToaNha)
        if (user.getRole() == Role.ACCOUNTANT || user.getRole() == Role.RESIDENT) {
            return userToaNhaRepo.findToaNhaIdsByUserId(user.getId());
        }

        return List.of();
    }

    /**
     * Kiểm tra user có quyền truy cập tòa nhà cụ thể không
     */
    public boolean canAccessBuilding(Integer toaNhaId) {
        if (toaNhaId == null) return false;
        
        UserAccount user = getCurrentUser();
        if (user == null) return false;

        // Admin truy cập tất cả
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Manager chỉ truy cập tòa nhà mình sở hữu
        if (user.getRole() == Role.MANAGER) {
            return toaNhaRepo.findById(toaNhaId)
                .map(t -> t.getNguoiQuanLy() != null && t.getNguoiQuanLy().getId().equals(user.getId()))
                .orElse(false);
        }

        // ACCOUNTANT/RESIDENT truy cập tòa nhà được gắn vào (qua UserToaNha)
        if (user.getRole() == Role.ACCOUNTANT || user.getRole() == Role.RESIDENT) {
            return userToaNhaRepo.existsByUserIdAndToaNhaId(user.getId(), toaNhaId);
        }

        return false;
    }

    /**
     * Kiểm tra user có quyền quản lý (CRUD) tòa nhà cụ thể không
     * - ADMIN: Tất cả
     * - MANAGER: Chỉ tòa nhà của mình
     */
    public boolean canManageBuilding(Integer toaNhaId) {
        if (toaNhaId == null) return false;
        
        UserAccount user = getCurrentUser();
        if (user == null) return false;

        // Admin quản lý tất cả
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Manager chỉ quản lý tòa nhà của mình
        if (user.getRole() == Role.MANAGER) {
            return toaNhaRepo.findById(toaNhaId)
                .map(t -> t.getNguoiQuanLy() != null && t.getNguoiQuanLy().getId().equals(user.getId()))
                .orElse(false);
        }

        return false;
    }
}
