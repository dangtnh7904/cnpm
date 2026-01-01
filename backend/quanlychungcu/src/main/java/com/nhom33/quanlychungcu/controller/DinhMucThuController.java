package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.DinhMucThu;
import com.nhom33.quanlychungcu.service.DinhMucThuService;
import com.nhom33.quanlychungcu.service.DinhMucThuService.DinhMucWithPrice;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller: Quản lý Định Mức Thu.
 * 
 * API ENDPOINTS:
 * - POST   /api/dinh-muc-thu                      : Tạo định mức mới
 * - PUT    /api/dinh-muc-thu/{id}                 : Cập nhật định mức
 * - DELETE /api/dinh-muc-thu/{id}                 : Xóa định mức
 * - GET    /api/dinh-muc-thu/ho-gia-dinh/{id}     : Lấy định mức của hộ gia đình
 * - GET    /api/dinh-muc-thu/ho-gia-dinh/{id}/with-price : Lấy định mức kèm giá
 * - POST   /api/dinh-muc-thu/bulk/toa-nha/{id}    : Tạo hàng loạt theo tòa nhà
 */
@RestController
@RequestMapping("/api/dinh-muc-thu")
public class DinhMucThuController {

    private final DinhMucThuService service;

    public DinhMucThuController(DinhMucThuService service) {
        this.service = service;
    }

    // ===== CREATE =====

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DinhMucThu> create(@Valid @RequestBody DinhMucThu dinhMuc) {
        DinhMucThu created = service.create(dinhMuc);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Tạo định mức hàng loạt cho tất cả hộ gia đình trong tòa nhà.
     */
    @PostMapping("/bulk/toa-nha/{toaNhaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBulkByToaNha(
            @PathVariable @NonNull Integer toaNhaId,
            @RequestParam @NonNull Integer loaiPhiId,
            @RequestParam(defaultValue = "1.0") Double soLuong) {
        
        int count = service.createBulkByToaNha(toaNhaId, loaiPhiId, soLuong);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã tạo định mức cho " + count + " hộ gia đình");
        response.put("soLuong", count);
        
        return ResponseEntity.ok(response);
    }

    // ===== UPDATE =====

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DinhMucThu> update(
            @PathVariable @NonNull Integer id,
            @Valid @RequestBody DinhMucThu dinhMuc) {
        DinhMucThu updated = service.update(id, dinhMuc);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/so-luong")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DinhMucThu> updateSoLuong(
            @PathVariable @NonNull Integer id,
            @RequestParam Double soLuong) {
        DinhMucThu updated = service.updateSoLuong(id, soLuong);
        return ResponseEntity.ok(updated);
    }

    /**
     * Cập nhật số lượng hàng loạt cho tất cả hộ gia đình trong tòa nhà.
     */
    @PatchMapping("/bulk/toa-nha/{toaNhaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBulkByToaNha(
            @PathVariable @NonNull Integer toaNhaId,
            @RequestParam @NonNull Integer loaiPhiId,
            @RequestParam @NonNull Double soLuong) {
        
        int count = service.updateBulkByToaNha(toaNhaId, loaiPhiId, soLuong);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã cập nhật " + count + " định mức");
        response.put("soLuong", count);
        
        return ResponseEntity.ok(response);
    }

    // ===== DELETE =====

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa định mức thu thành công");
        return ResponseEntity.ok(response);
    }

    // ===== READ =====

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<DinhMucThu> getById(@PathVariable @NonNull Integer id) {
        DinhMucThu dinhMuc = service.getById(id);
        return ResponseEntity.ok(dinhMuc);
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<DinhMucThu>> findByHoGiaDinh(@PathVariable @NonNull Integer idHoGiaDinh) {
        List<DinhMucThu> result = service.findByHoGiaDinh(idHoGiaDinh);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}/page")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<DinhMucThu>> findByHoGiaDinhPage(
            @PathVariable @NonNull Integer idHoGiaDinh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DinhMucThu> result = service.findByHoGiaDinh(idHoGiaDinh, pageable);
        return ResponseEntity.ok(result);
    }

    // ===== PRICE CALCULATION =====

    /**
     * Lấy danh sách định mức của hộ gia đình kèm giá đã tính.
     * Response bao gồm: định mức, đơn giá áp dụng, thành tiền, và flag giá riêng.
     */
    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}/with-price")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<DinhMucWithPrice>> findByHoGiaDinhWithPrice(
            @PathVariable @NonNull Integer idHoGiaDinh) {
        List<DinhMucWithPrice> result = service.findByHoGiaDinhWithPrice(idHoGiaDinh);
        return ResponseEntity.ok(result);
    }

    /**
     * Tính tổng tiền của một hộ gia đình.
     */
    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}/tong-tien")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> getTongTien(@PathVariable @NonNull Integer idHoGiaDinh) {
        BigDecimal tongTien = service.tinhTongTien(idHoGiaDinh);
        
        Map<String, Object> response = new HashMap<>();
        response.put("hoGiaDinhId", idHoGiaDinh);
        response.put("tongTien", tongTien);
        
        return ResponseEntity.ok(response);
    }
}

