package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.entity.UserAccount;
import com.nhom33.quanlychungcu.entity.Role;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;
import com.nhom33.quanlychungcu.repository.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ToaNhaService {

    private final ToaNhaRepository repo;
    private final UserAccountRepository userRepo;
    private final SecurityHelper securityHelper;

    public ToaNhaService(ToaNhaRepository repo, UserAccountRepository userRepo, SecurityHelper securityHelper) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.securityHelper = securityHelper;
    }

    /**
     * Lấy user đang đăng nhập
     */
    private UserAccount getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElse(null);
    }

    /**
     * Kiểm tra user có phải Admin hệ thống không
     */
    private boolean isSystemAdmin(UserAccount user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    /**
     * Kiểm tra user có phải Manager không
     */
    private boolean isManager(UserAccount user) {
        return user != null && user.getRole() == Role.MANAGER;
    }

    /**
     * Kiểm tra user có quyền xem tất cả không (Admin hoặc Accountant)
     * - ADMIN: Quản trị hệ thống, xem tất cả
     * - ACCOUNTANT: Kế toán, cần xem tất cả để làm báo cáo
     * - MANAGER: Chỉ xem tòa nhà của mình
     */
    private boolean canViewAll(UserAccount user) {
        return user != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.ACCOUNTANT);
    }

    @Transactional
    public ToaNha create(ToaNha toaNha) {
        // Multi-tenancy: Không cần kiểm tra trùng tên tòa nhà
        // Mỗi manager quản lý tòa nhà riêng, có thể đặt tên tùy ý
        
        // Tự động gán người quản lý là user đang login (nếu chưa set)
        if (toaNha.getNguoiQuanLy() == null) {
            UserAccount currentUser = getCurrentUser();
            if (currentUser != null) {
                toaNha.setNguoiQuanLy(currentUser);
            }
        }
        
        return repo.save(toaNha);
    }

    @Transactional
    public ToaNha update(@NonNull Integer id, ToaNha updated) {
        ToaNha exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + id));

        // Kiểm tra quyền: chỉ người quản lý hoặc Admin mới được sửa
        UserAccount currentUser = getCurrentUser();
        if (!isSystemAdmin(currentUser) && 
            (exist.getNguoiQuanLy() == null || !exist.getNguoiQuanLy().getId().equals(currentUser.getId()))) {
            throw new SecurityException("Bạn không có quyền sửa tòa nhà này");
        }

        // Multi-tenancy: Không cần kiểm tra trùng tên tòa nhà

        // Cập nhật thông tin
        exist.setTenToaNha(updated.getTenToaNha());
        exist.setMoTa(updated.getMoTa());

        return repo.save(exist);
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        ToaNha exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + id));

        // Kiểm tra quyền
        UserAccount currentUser = getCurrentUser();
        if (!isSystemAdmin(currentUser) && 
            (exist.getNguoiQuanLy() == null || !exist.getNguoiQuanLy().getId().equals(currentUser.getId()))) {
            throw new SecurityException("Bạn không có quyền xóa tòa nhà này");
        }

        repo.deleteById(id);
    }

    public ToaNha getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + id));
    }

    /**
     * Lấy tất cả tòa nhà - có lọc theo người quản lý nếu là Manager
     * - ADMIN/ACCOUNTANT: Xem tất cả
     * - MANAGER: Chỉ xem tòa nhà mình quản lý
     */
    public List<ToaNha> getAll() {
        UserAccount currentUser = getCurrentUser();
        
        // Admin/Accountant thấy tất cả
        if (canViewAll(currentUser)) {
            return repo.findAll();
        }
        
        // Manager chỉ thấy tòa nhà của mình
        if (isManager(currentUser)) {
            return repo.findByNguoiQuanLyId(currentUser.getId());
        }
        
        return List.of();
    }

    /**
     * Lấy tòa nhà theo người quản lý cụ thể
     */
    public List<ToaNha> getByNguoiQuanLy(Integer nguoiQuanLyId) {
        return repo.findByNguoiQuanLyId(nguoiQuanLyId);
    }

    public Page<ToaNha> findAll(@NonNull Pageable pageable) {
        UserAccount currentUser = getCurrentUser();
        
        if (canViewAll(currentUser)) {
            return repo.findAll(pageable);
        }
        
        if (isManager(currentUser)) {
            return repo.findByNguoiQuanLy(currentUser, pageable);
        }
        
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public Page<ToaNha> searchByTenToaNha(String tenToaNha, @NonNull Pageable pageable) {
        UserAccount currentUser = getCurrentUser();
        
        if (tenToaNha == null || tenToaNha.isBlank()) {
            return findAll(pageable);
        }
        
        if (canViewAll(currentUser)) {
            return repo.findByTenToaNhaContainingIgnoreCase(tenToaNha, pageable);
        }
        
        if (isManager(currentUser)) {
            return repo.findByNguoiQuanLyAndTenToaNhaContainingIgnoreCase(currentUser, tenToaNha, pageable);
        }
        
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    /**
     * Lấy tòa nhà của user hiện tại (qua UserToaNha).
     * Dùng cho RESIDENT gửi phản ánh / xem thông báo theo tòa nhà mình thuộc.
     */
    public List<ToaNha> getMyBuildings() {
        List<Integer> toaNhaIds = securityHelper.getAccessibleBuildingIds();
        if (toaNhaIds.isEmpty()) {
            return List.of();
        }
        return repo.findAllById(toaNhaIds);
    }
}
