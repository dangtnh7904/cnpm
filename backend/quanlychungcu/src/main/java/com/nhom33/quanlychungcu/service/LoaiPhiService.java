package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.LoaiPhiRequestDTO;
import com.nhom33.quanlychungcu.entity.LoaiPhi;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.BangGiaDichVuRepository;
import com.nhom33.quanlychungcu.repository.LoaiPhiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service: Quản lý Loại Phí.
 * 
 * LOGIC NGHIỆP VỤ:
 * - DonGia trong LoaiPhi là giá mặc định (Base Price).
 * - Giá riêng theo tòa nhà được cấu hình qua BangGiaDichVu.
 * - Hỗ trợ soft delete thông qua trường dangHoatDong.
 */
@Service
public class LoaiPhiService {

    private final LoaiPhiRepository repo;
    private final BangGiaDichVuRepository bangGiaRepo;

    public LoaiPhiService(LoaiPhiRepository repo, BangGiaDichVuRepository bangGiaRepo) {
        this.repo = repo;
        this.bangGiaRepo = bangGiaRepo;
    }

    // ===== CREATE =====

    @Transactional
    public LoaiPhi create(LoaiPhi loaiPhi) {
        return repo.save(loaiPhi);
    }

    /**
     * Tạo loại phí từ DTO.
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
        
        return repo.save(loaiPhi);
    }

    // ===== UPDATE =====

    @Transactional
    public LoaiPhi update(@NonNull Integer id, LoaiPhi updated) {
        LoaiPhi exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
        exist.setTenLoaiPhi(updated.getTenLoaiPhi());
        exist.setDonGia(updated.getDonGia());
        exist.setDonViTinh(updated.getDonViTinh());
        exist.setLoaiThu(updated.getLoaiThu());
        exist.setMoTa(updated.getMoTa());
        exist.setDangHoatDong(updated.getDangHoatDong());
        
        return repo.save(exist);
    }

    /**
     * Cập nhật loại phí từ DTO.
     */
    @Transactional
    public LoaiPhi updateFromDTO(@NonNull Integer id, LoaiPhiRequestDTO dto) {
        LoaiPhi exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
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
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id);
        }
        
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
        
        exist.setDangHoatDong(true);
        return repo.save(exist);
    }

    // ===== READ =====

    public LoaiPhi getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
    }

    public Page<LoaiPhi> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<LoaiPhi> findByDangHoatDong(Boolean dangHoatDong, @NonNull Pageable pageable) {
        return repo.findByDangHoatDong(dangHoatDong, pageable);
    }

    public Page<LoaiPhi> search(String tenLoaiPhi, String loaiThu, Boolean dangHoatDong, @NonNull Pageable pageable) {
        return repo.search(tenLoaiPhi, loaiThu, dangHoatDong, pageable);
    }

    public List<LoaiPhi> findAllActive() {
        return repo.findByDangHoatDongTrue();
    }

    /**
     * Lấy tất cả loại phí (không phân trang).
     */
    public List<LoaiPhi> findAll() {
        return repo.findAll();
    }

    /**
     * Kiểm tra loại phí có tồn tại không.
     */
    public boolean existsById(@NonNull Integer id) {
        return repo.existsById(id);
    }
}

