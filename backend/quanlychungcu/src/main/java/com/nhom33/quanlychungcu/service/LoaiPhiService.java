package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.LoaiPhiRequestDTO;
import com.nhom33.quanlychungcu.entity.LoaiPhi;
import com.nhom33.quanlychungcu.entity.UserAccount;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.BangGiaDichVuRepository;
import com.nhom33.quanlychungcu.repository.LoaiPhiRepository;
import com.nhom33.quanlychungcu.repository.UserAccountRepository;
// SecurityHelper is in the same package
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service: Quản lý Loại Phí.
 * 
 * LOGIC NGHIỆP VỤ:
 * - DonGia trong LoaiPhi là giá mặc định (Base Price).
 * - Giá riêng theo tòa nhà được cấu hình qua BangGiaDichVu.
 * - Hỗ trợ soft delete thông qua trường dangHoatDong.
 * 
 * MULTI-TENANCY v4.1 - PHÍ CHUNG CỦA MANAGER:
 * - Loại phí thuộc về Manager (nguoiQuanLy), không thuộc tòa nhà
 * - toaNha = NULL: phí CHUNG (áp dụng cho tất cả tòa của Manager)
 * - toaNha != NULL: phí RIÊNG chỉ cho tòa đó
 * - Manager chỉ xem/quản lý phí của mình
 * - Giá riêng cho từng tòa qua BangGiaDichVu
 */
@Service
public class LoaiPhiService {

    private final LoaiPhiRepository repo;
    private final BangGiaDichVuRepository bangGiaRepo;
    private final UserAccountRepository userRepo;
    private final SecurityHelper securityHelper;

    public LoaiPhiService(LoaiPhiRepository repo, BangGiaDichVuRepository bangGiaRepo, 
                          UserAccountRepository userRepo, SecurityHelper securityHelper) {
        this.repo = repo;
        this.bangGiaRepo = bangGiaRepo;
        this.userRepo = userRepo;
        this.securityHelper = securityHelper;
    }
    
    // ===== Multi-tenancy helper methods =====
    
    /**
     * Kiểm tra quyền truy cập loại phí.
     * Multi-tenancy v4.1: Chỉ Manager sở hữu hoặc Admin mới được truy cập.
     */
    private void checkFeeAccess(LoaiPhi loaiPhi) {
        if (securityHelper.canViewAll()) {
            return; // Admin xem tất cả
        }
        
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (!loaiPhi.getNguoiQuanLy().getId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Bạn không có quyền truy cập loại phí này");
        }
    }
    
    /**
     * Kiểm tra quyền quản lý loại phí (tạo/sửa/xóa).
     * Multi-tenancy v4.1: Chỉ Manager sở hữu mới được quản lý.
     */
    private void checkFeeManagePermission(LoaiPhi loaiPhi) {
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (!loaiPhi.getNguoiQuanLy().getId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Bạn không có quyền quản lý loại phí này");
        }
    }
    
    /**
     * Lấy Manager hiện tại.
     */
    private UserAccount getCurrentManager() {
        Integer userId = securityHelper.getCurrentUserId();
        return userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    // ===== CREATE =====

    /**
     * Tạo loại phí CHUNG cho Manager hiện tại.
     * Phí chung không gắn với tòa nhà cụ thể (toaNha = NULL).
     */
    @Transactional
    public LoaiPhi create(LoaiPhi loaiPhi) {
        // Gắn Manager sở hữu
        loaiPhi.setNguoiQuanLy(getCurrentManager());
        // Phí chung: toaNha = NULL (đã được set từ client hoặc để null)
        return repo.save(loaiPhi);
    }

    /**
     * Tạo loại phí từ DTO - Phí CHUNG của Manager.
     */
    @Transactional
    public LoaiPhi createFromDTO(LoaiPhiRequestDTO dto) {
        LoaiPhi loaiPhi = new LoaiPhi();
        loaiPhi.setTenLoaiPhi(dto.getTenLoaiPhi());
        loaiPhi.setDonGia(dto.getDonGia());
        loaiPhi.setDonViTinh(dto.getDonViTinh());
        loaiPhi.setLoaiThu(dto.getLoai());
        loaiPhi.setMoTa(dto.getMoTa());
        loaiPhi.setDangHoatDong(true);
        // Gắn Manager sở hữu
        loaiPhi.setNguoiQuanLy(getCurrentManager());
        // Phí chung: toaNha = NULL
        loaiPhi.setToaNha(null);
        
        return repo.save(loaiPhi);
    }

    // ===== UPDATE =====

    @Transactional
    public LoaiPhi update(@NonNull Integer id, LoaiPhi updated) {
        LoaiPhi exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
        // Kiểm tra quyền
        checkFeeManagePermission(exist);
        
        exist.setTenLoaiPhi(updated.getTenLoaiPhi());
        exist.setDonGia(updated.getDonGia());
        exist.setDonViTinh(updated.getDonViTinh());
        exist.setLoaiThu(updated.getLoaiThu());
        exist.setMoTa(updated.getMoTa());
        exist.setDangHoatDong(updated.getDangHoatDong());
        // Không thay đổi nguoiQuanLy
        
        return repo.save(exist);
    }

    /**
     * Cập nhật loại phí từ DTO.
     */
    @Transactional
    public LoaiPhi updateFromDTO(@NonNull Integer id, LoaiPhiRequestDTO dto) {
        LoaiPhi exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
        // Kiểm tra quyền
        checkFeeManagePermission(exist);
        
        exist.setTenLoaiPhi(dto.getTenLoaiPhi());
        exist.setDonGia(dto.getDonGia());
        exist.setDonViTinh(dto.getDonViTinh());
        exist.setLoaiThu(dto.getLoai());
        exist.setMoTa(dto.getMoTa());
        
        return repo.save(exist);
    }

    /**
     * Cập nhật đơn giá mặc định.
     */
    @Transactional
    public LoaiPhi updateDonGia(@NonNull Integer id, BigDecimal donGia) {
        LoaiPhi exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
        // Kiểm tra quyền
        checkFeeManagePermission(exist);
        
        exist.setDonGia(donGia);
        return repo.save(exist);
    }

    // ===== DELETE =====

    /**
     * Hard delete - Xóa vĩnh viễn.
     * Cũng xóa tất cả bảng giá liên quan.
     */
    @Transactional
    public void delete(@NonNull Integer id) {
        LoaiPhi exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
        // Kiểm tra quyền
        checkFeeManagePermission(exist);
        
        // Xóa tất cả bảng giá liên quan trước
        bangGiaRepo.deleteByLoaiPhiId(id);
        
        repo.deleteById(id);
    }

    /**
     * Soft delete - Đánh dấu không hoạt động.
     * Giữ lại dữ liệu lịch sử.
     */
    @Transactional
    public LoaiPhi softDelete(@NonNull Integer id) {
        LoaiPhi exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
        // Kiểm tra quyền
        checkFeeManagePermission(exist);
        
        exist.setDangHoatDong(false);
        return repo.save(exist);
    }

    /**
     * Khôi phục loại phí đã soft delete.
     */
    @Transactional
    public LoaiPhi restore(@NonNull Integer id) {
        LoaiPhi exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
        // Kiểm tra quyền
        checkFeeManagePermission(exist);
        
        exist.setDangHoatDong(true);
        return repo.save(exist);
    }

    // ===== READ =====

    public LoaiPhi getById(@NonNull Integer id) {
        LoaiPhi loaiPhi = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
        // Multi-tenancy: Kiểm tra quyền xem
        checkFeeAccess(loaiPhi);
        
        return loaiPhi;
    }

    /**
     * Lấy tất cả loại phí của Manager hiện tại.
     * Multi-tenancy v4.1: Lọc theo nguoiQuanLy.
     */
    public Page<LoaiPhi> findAll(@NonNull Pageable pageable) {
        // ADMIN xem tất cả
        if (securityHelper.canViewAll()) {
            return repo.findAll(pageable);
        }
        
        // Manager xem phí của mình
        Integer managerId = securityHelper.getCurrentUserId();
        return repo.findByNguoiQuanLyId(managerId, pageable);
    }

    /**
     * Lấy tất cả loại phí CHUNG của Manager (không gắn tòa cụ thể).
     */
    public Page<LoaiPhi> findPhiChung(@NonNull Pageable pageable) {
        Integer managerId = securityHelper.getCurrentUserId();
        return repo.findPhiChungByNguoiQuanLy(managerId, pageable);
    }

    /**
     * Lấy tất cả loại phí áp dụng cho một tòa nhà.
     * Bao gồm: phí CHUNG của Manager + phí RIÊNG của tòa.
     */
    public Page<LoaiPhi> findByToaNha(Integer toaNhaId, @NonNull Pageable pageable) {
        Integer managerId = securityHelper.getCurrentUserId();
        return repo.findByNguoiQuanLyAndToaNha(managerId, toaNhaId, pageable);
    }

    /**
     * Lấy tất cả loại phí đang hoạt động áp dụng cho một tòa nhà.
     */
    public List<LoaiPhi> findActiveByToaNha(Integer toaNhaId) {
        Integer managerId = securityHelper.getCurrentUserId();
        return repo.findActiveByNguoiQuanLyAndToaNha(managerId, toaNhaId);
    }

    public Page<LoaiPhi> findByDangHoatDong(Boolean dangHoatDong, @NonNull Pageable pageable) {
        return repo.findByDangHoatDong(dangHoatDong, pageable);
    }

    /**
     * Tìm kiếm loại phí của Manager hiện tại.
     */
    public Page<LoaiPhi> search(String tenLoaiPhi, String loaiThu, Boolean dangHoatDong, @NonNull Pageable pageable) {
        // ADMIN xem tất cả
        if (securityHelper.canViewAll()) {
            return repo.search(tenLoaiPhi, loaiThu, dangHoatDong, pageable);
        }
        
        // Manager tìm trong phí của mình
        Integer managerId = securityHelper.getCurrentUserId();
        return repo.searchByNguoiQuanLy(managerId, tenLoaiPhi, loaiThu, dangHoatDong, pageable);
    }

    /**
     * Lấy tất cả loại phí đang hoạt động của Manager.
     */
    public List<LoaiPhi> findAllActive() {
        // ADMIN xem tất cả
        if (securityHelper.canViewAll()) {
            return repo.findByDangHoatDongTrue();
        }
        
        // Manager lấy phí chung đang hoạt động của mình
        Integer managerId = securityHelper.getCurrentUserId();
        return repo.findPhiChungActiveByNguoiQuanLy(managerId);
    }

    /**
     * Lấy tất cả loại phí (không phân trang).
     */
    public List<LoaiPhi> findAll() {
        // ADMIN xem tất cả
        if (securityHelper.canViewAll()) {
            return repo.findAll();
        }
        
        // Manager xem phí của mình
        Integer managerId = securityHelper.getCurrentUserId();
        return repo.findByNguoiQuanLyId(managerId, Pageable.unpaged()).getContent();
    }

    /**
     * Kiểm tra loại phí có tồn tại không.
     */
    public boolean existsById(@NonNull Integer id) {
        return repo.existsById(id);
    }
}

