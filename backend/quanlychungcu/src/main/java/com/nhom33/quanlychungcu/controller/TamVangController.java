package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.dto.DangKyTamVangDTO;
import com.nhom33.quanlychungcu.entity.TamVang;
import com.nhom33.quanlychungcu.service.TamVangService;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý API Tạm vắng.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Tạm vắng = Người ĐÃ LÀ THÀNH VIÊN của hộ đi vắng tạm thời.
 * - Khi đăng ký: Update NhanKhau.TrangThai = "Tạm vắng" + Insert TamVang.
 * - KHÔNG thêm mới nhân khẩu, chỉ cập nhật trạng thái.
 */
@RestController
@RequestMapping("/api/tam-vang")
public class TamVangController {

    private final TamVangService service;

    public TamVangController(TamVangService service) {
        this.service = service;
    }

    // ===================================================================
    //  ĐĂNG KÝ TẠM VẮNG (CORE API)
    // ===================================================================

    /**
     * Đăng ký tạm vắng cho nhân khẩu (người ở đây đi vắng).
     * 
     * Luồng xử lý:
     * 1. Validate NhanKhau: Phải tồn tại và TrangThai = "Đang ở"/"Thường trú"
     * 2. Update NhanKhau.TrangThai = "Tạm vắng"
     * 3. Insert TamVang liên kết với NhanKhau
     * 
     * POST /api/tam-vang/dang-ky
     */
    @PostMapping("/dang-ky")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TamVang> dangKyTamVang(@Valid @RequestBody DangKyTamVangDTO dto) {
        TamVang result = service.dangKyTamVang(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Kết thúc tạm vắng (nhân khẩu đã trở về).
     * Cập nhật NhanKhau.TrangThai = "Đang ở".
     * 
     * POST /api/tam-vang/{id}/ket-thuc
     */
    @PostMapping("/{id}/ket-thuc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> ketThucTamVang(@PathVariable @NonNull Integer id) {
        service.ketThucTamVang(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã kết thúc tạm vắng thành công");
        return ResponseEntity.ok(response);
    }

    // ===================================================================
    //  TRA CỨU
    // ===================================================================

    /**
     * Lấy thông tin tạm vắng theo ID.
     * 
     * GET /api/tam-vang/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TamVang> getById(@PathVariable @NonNull Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Tìm kiếm tạm vắng (theo nơi đến).
     * 
     * GET /api/tam-vang?noiDen=xxx&page=0&size=10
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TamVang>> search(
            @RequestParam(required = false) String noiDen,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        return ResponseEntity.ok(service.searchByNoiDen(noiDen, p));
    }

    /**
     * Tìm tạm vắng theo nhân khẩu.
     * 
     * GET /api/tam-vang/nhan-khau/{nhanKhauId}
     */
    @GetMapping("/nhan-khau/{nhanKhauId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TamVang>> findByNhanKhau(
            @PathVariable @NonNull Integer nhanKhauId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        return ResponseEntity.ok(service.findByNhanKhauId(nhanKhauId, p));
    }

    /**
     * Tìm tạm vắng theo hộ gia đình.
     * 
     * GET /api/tam-vang/ho-gia-dinh/{hoGiaDinhId}
     */
    @GetMapping("/ho-gia-dinh/{hoGiaDinhId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TamVang>> findByHoGiaDinh(
            @PathVariable @NonNull Integer hoGiaDinhId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        return ResponseEntity.ok(service.findByHoGiaDinhId(hoGiaDinhId, p));
    }

    // ===================================================================
    //  XÓA (ADMIN ONLY)
    // ===================================================================

    /**
     * Xóa bản ghi tạm vắng.
     * Tự động khôi phục trạng thái nhân khẩu nếu cần.
     * 
     * DELETE /api/tam-vang/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
