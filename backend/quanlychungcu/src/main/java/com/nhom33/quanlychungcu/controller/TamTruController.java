package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.dto.DangKyTamTruDTO;
import com.nhom33.quanlychungcu.entity.TamTru;
import com.nhom33.quanlychungcu.service.TamTruService;
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
 * Controller xử lý API Tạm trú.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Tạm trú = Người từ nơi khác đến ở tạm tại hộ gia đình.
 * - Khi đăng ký tạm trú: Insert NhanKhau (TrangThai="Tạm trú") + Insert TamTru.
 * - Người tạm trú PHẢI xuất hiện trong danh sách nhân khẩu của hộ.
 */
@RestController
@RequestMapping("/api/tam-tru")
public class TamTruController {

    private final TamTruService service;

    public TamTruController(TamTruService service) {
        this.service = service;
    }

    // ===================================================================
    //  ĐĂNG KÝ TẠM TRÚ (CORE API)
    // ===================================================================

    /**
     * Đăng ký tạm trú cho người nơi khác đến ở.
     * 
     * Luồng xử lý:
     * 1. Insert NhanKhau với TrangThai = "Tạm trú"
     * 2. Insert TamTru liên kết với NhanKhau vừa tạo
     * 
     * POST /api/tam-tru/dang-ky
     */
    @PostMapping("/dang-ky")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TamTru> dangKyTamTru(@Valid @RequestBody DangKyTamTruDTO dto) {
        TamTru result = service.dangKyTamTru(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Hủy tạm trú (khi người tạm trú rời đi hoặc hết hạn).
     * Cập nhật NhanKhau.TrangThai = "Đã chuyển đi".
     * 
     * POST /api/tam-tru/{id}/huy
     */
    @PostMapping("/{id}/huy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> huyTamTru(@PathVariable @NonNull Integer id) {
        service.huyTamTru(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã hủy tạm trú thành công");
        return ResponseEntity.ok(response);
    }

    // ===================================================================
    //  TRA CỨU
    // ===================================================================

    /**
     * Lấy thông tin tạm trú theo ID.
     * 
     * GET /api/tam-tru/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TamTru> getById(@PathVariable @NonNull Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Tìm kiếm tạm trú (theo tên nhân khẩu).
     * 
     * GET /api/tam-tru?hoTen=xxx&page=0&size=10
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TamTru>> search(
            @RequestParam(required = false) String hoTen,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        return ResponseEntity.ok(service.searchByNhanKhauName(hoTen, p));
    }

    /**
     * Tìm tạm trú theo hộ gia đình.
     * 
     * GET /api/tam-tru/ho-gia-dinh/{hoGiaDinhId}
     */
    @GetMapping("/ho-gia-dinh/{hoGiaDinhId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TamTru>> findByHoGiaDinh(
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
     * Xóa bản ghi tạm trú.
     * Lưu ý: Sử dụng POST /{id}/huy nếu muốn cập nhật trạng thái nhân khẩu.
     * 
     * DELETE /api/tam-tru/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
