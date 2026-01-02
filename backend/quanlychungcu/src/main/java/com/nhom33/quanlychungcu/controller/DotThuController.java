package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.DotThu;
import com.nhom33.quanlychungcu.entity.DotThuLoaiPhi;
import com.nhom33.quanlychungcu.service.DotThuService;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dot-thu")
public class DotThuController {

    private final DotThuService service;

    public DotThuController(DotThuService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DotThu> create(@Valid @RequestBody DotThu dotThu) {
        DotThu created = service.create(dotThu);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DotThu> update(
            @PathVariable @NonNull Integer id,
            @Valid @RequestBody DotThu dotThu) {
        DotThu updated = service.update(id, dotThu);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa đợt thu thành công");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<DotThu> getById(@PathVariable @NonNull Integer id) {
        DotThu dotThu = service.getById(id);
        return ResponseEntity.ok(dotThu);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<DotThu>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DotThu> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<DotThu>> search(
            @RequestParam(required = false) String tenDotThu,
            @RequestParam(required = false) String loaiDotThu,
            @RequestParam(required = false) Integer toaNhaId,
            @RequestParam(required = false) LocalDate ngayBatDau,
            @RequestParam(required = false) LocalDate ngayKetThuc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DotThu> result = service.search(tenDotThu, loaiDotThu, toaNhaId, ngayBatDau, ngayKetThuc, pageable);
        return ResponseEntity.ok(result);
    }
    
    // ===== Quản lý loại phí trong đợt thu =====
    
    /**
     * Lấy danh sách loại phí trong đợt thu.
     */
    @GetMapping("/{id}/fees")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<DotThuLoaiPhi>> getFeesInPeriod(@PathVariable @NonNull Integer id) {
        List<DotThuLoaiPhi> fees = service.getFeesInPeriod(id);
        return ResponseEntity.ok(fees);
    }
    
    /**
     * Kiểm tra đợt thu có chứa phí biến đổi (Điện/Nước) không.
     * Dùng để Frontend quyết định hiển thị Tab Ghi Chỉ Số.
     */
    @GetMapping("/{id}/has-utility-fee")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, Boolean>> hasUtilityFee(@PathVariable @NonNull Integer id) {
        boolean hasUtility = service.checkHasUtilityFee(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasUtilityFee", hasUtility);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy danh sách phí biến đổi (Điện/Nước) trong đợt thu.
     */
    @GetMapping("/{id}/utility-fees")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<DotThuLoaiPhi>> getUtilityFees(@PathVariable @NonNull Integer id) {
        List<DotThuLoaiPhi> fees = service.getUtilityFeesInPeriod(id);
        return ResponseEntity.ok(fees);
    }
    
    /**
     * Thêm loại phí vào đợt thu.
     * Response có flag hasUtilityFee để Frontend biết có cần hiện Tab Ghi Chỉ Số không.
     */
    @PostMapping("/{id}/fees/{loaiPhiId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addFeeToPeriod(
            @PathVariable @NonNull Integer id,
            @PathVariable @NonNull Integer loaiPhiId) {
        Map<String, Object> result = service.addFeeToPeriod(id, loaiPhiId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /**
     * Xóa loại phí khỏi đợt thu.
     * Không cho xóa phí bắt buộc (Điện, Nước).
     * Response có flag hasUtilityFee sau khi xóa.
     */
    @DeleteMapping("/{id}/fees/{loaiPhiId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removeFeeFromPeriod(
            @PathVariable @NonNull Integer id,
            @PathVariable @NonNull Integer loaiPhiId) {
        Map<String, Object> result = service.removeFeeFromPeriod(id, loaiPhiId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Kiểm tra loại phí có phải bắt buộc không.
     */
    @GetMapping("/fees/{loaiPhiId}/is-mandatory")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, Boolean>> isMandatoryFee(@PathVariable @NonNull Integer loaiPhiId) {
        boolean isMandatory = service.isMandatoryFee(loaiPhiId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isMandatory", isMandatory);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kiểm tra loại phí có phải phí biến đổi (cần ghi chỉ số) không.
     */
    @GetMapping("/fees/{loaiPhiId}/is-utility")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, Boolean>> isUtilityFee(@PathVariable @NonNull Integer loaiPhiId) {
        boolean isUtility = service.isUtilityFee(loaiPhiId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isUtility", isUtility);
        return ResponseEntity.ok(response);
    }
}

