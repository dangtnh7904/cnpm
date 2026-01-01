package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.dto.BangGiaResponseDTO;
import com.nhom33.quanlychungcu.dto.CauHinhGiaDTO;
import com.nhom33.quanlychungcu.dto.ChiTietGiaDTO;
import com.nhom33.quanlychungcu.entity.BangGiaDichVu;
import com.nhom33.quanlychungcu.service.BangGiaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller: Quản lý Bảng giá dịch vụ theo tòa nhà.
 * 
 * API ENDPOINTS:
 * - POST /api/bang-gia/cau-hinh     : Cấu hình giá hàng loạt cho một tòa nhà
 * - POST /api/bang-gia/upsert       : Upsert một bảng giá đơn lẻ
 * - GET  /api/bang-gia              : Lấy tất cả bảng giá
 * - GET  /api/bang-gia/toa-nha/{id} : Lấy bảng giá của một tòa nhà
 * - GET  /api/bang-gia/don-gia      : Lấy đơn giá áp dụng
 * - DELETE /api/bang-gia/{id}       : Xóa một bảng giá
 */
@RestController
@RequestMapping("/api/bang-gia")
public class BangGiaController {

    private final BangGiaService bangGiaService;

    public BangGiaController(BangGiaService bangGiaService) {
        this.bangGiaService = bangGiaService;
    }

    // ===== BULK UPSERT =====

    /**
     * Cấu hình giá hàng loạt cho một tòa nhà.
     * 
     * REQUEST BODY:
     * {
     *   "toaNhaId": 1,
     *   "danhSachGia": [
     *     { "loaiPhiId": 1, "donGiaRieng": 50000 },
     *     { "loaiPhiId": 2, "donGiaRieng": 100000 }
     *   ]
     * }
     */
    @PostMapping("/cau-hinh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cauHinhGia(@Valid @RequestBody CauHinhGiaDTO cauHinhGiaDTO) {
        int count = bangGiaService.cauHinhGiaTheoToaNha(cauHinhGiaDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã cấu hình giá thành công");
        response.put("soLuong", count);
        response.put("toaNhaId", cauHinhGiaDTO.getToaNhaId());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Upsert một bảng giá đơn lẻ.
     * 
     * REQUEST BODY:
     * {
     *   "loaiPhiId": 1,
     *   "donGiaRieng": 50000,
     *   "ghiChu": "Giá ưu đãi tòa A"
     * }
     */
    @PostMapping("/upsert")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BangGiaDichVu> upsertBangGia(
            @RequestParam Integer toaNhaId,
            @Valid @RequestBody ChiTietGiaDTO chiTietGiaDTO) {
        
        BangGiaDichVu result = bangGiaService.upsertBangGia(
                chiTietGiaDTO.getLoaiPhiId(),
                toaNhaId,
                chiTietGiaDTO.getDonGiaRieng(),
                chiTietGiaDTO.getGhiChu());
        
        return ResponseEntity.ok(result);
    }

    // ===== READ =====

    /**
     * Lấy tất cả bảng giá.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<BangGiaDichVu>> findAll() {
        List<BangGiaDichVu> result = bangGiaService.findAll();
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy tất cả bảng giá của một tòa nhà (bao gồm loại phí chưa có giá riêng).
     */
    @GetMapping("/toa-nha/{toaNhaId}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<BangGiaResponseDTO>> findByToaNhaFull(@PathVariable Integer toaNhaId) {
        List<BangGiaResponseDTO> result = bangGiaService.getBangGiaFullByToaNha(toaNhaId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy bảng giá đã cấu hình của một tòa nhà.
     */
    @GetMapping("/toa-nha/{toaNhaId}/configured")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<BangGiaDichVu>> findByToaNha(@PathVariable Integer toaNhaId) {
        List<BangGiaDichVu> result = bangGiaService.findByToaNha(toaNhaId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy bảng giá của một loại phí (tại tất cả tòa nhà).
     */
    @GetMapping("/loai-phi/{loaiPhiId}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<BangGiaDichVu>> findByLoaiPhi(@PathVariable Integer loaiPhiId) {
        List<BangGiaDichVu> result = bangGiaService.findByLoaiPhi(loaiPhiId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy đơn giá áp dụng (với logic ưu tiên).
     * 
     * LOGIC:
     * 1. Nếu có giá riêng trong BangGiaDichVu -> trả về giá riêng.
     * 2. Nếu không có -> trả về giá mặc định từ LoaiPhi.
     */
    @GetMapping("/don-gia")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> getDonGiaApDung(
            @RequestParam Integer loaiPhiId,
            @RequestParam Integer toaNhaId) {
        
        BigDecimal donGia = bangGiaService.getDonGiaApDung(loaiPhiId, toaNhaId);
        
        // Kiểm tra có phải giá riêng không
        boolean hasCustomPrice = bangGiaService
                .findByLoaiPhiAndToaNha(loaiPhiId, toaNhaId)
                .isPresent();
        
        Map<String, Object> response = new HashMap<>();
        response.put("loaiPhiId", loaiPhiId);
        response.put("toaNhaId", toaNhaId);
        response.put("donGia", donGia);
        response.put("isCustomPrice", hasCustomPrice);
        
        return ResponseEntity.ok(response);
    }

    // ===== DELETE =====

    /**
     * Xóa một bảng giá theo ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Integer id) {
        bangGiaService.deleteById(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa bảng giá thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa bảng giá theo loại phí và tòa nhà.
     */
    @DeleteMapping("/toa-nha/{toaNhaId}/loai-phi/{loaiPhiId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteByLoaiPhiAndToaNha(
            @PathVariable Integer toaNhaId,
            @PathVariable Integer loaiPhiId) {
        
        bangGiaService.deleteByLoaiPhiAndToaNha(loaiPhiId, toaNhaId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa bảng giá thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Reset tất cả bảng giá của một tòa nhà về giá mặc định.
     */
    @DeleteMapping("/toa-nha/{toaNhaId}/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetToaNha(@PathVariable Integer toaNhaId) {
        bangGiaService.deleteByToaNha(toaNhaId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã reset giá về mặc định cho tòa nhà");
        return ResponseEntity.ok(response);
    }
}
