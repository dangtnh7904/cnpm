package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.ThongBao;
import com.nhom33.quanlychungcu.service.NotificationService;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // ===== CRUD Thông báo =====
    
    /**
     * Lấy danh sách thông báo (phân quyền tự động).
     */
    @GetMapping
    public ResponseEntity<Page<ThongBao>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        return ResponseEntity.ok(service.findAll(pageable));
    }

    /**
     * Tìm kiếm thông báo.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ThongBao>> search(
            @RequestParam(required = false) String loaiThongBao,
            @RequestParam(required = false) String tieuDe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        return ResponseEntity.ok(service.search(loaiThongBao, tieuDe, pageable));
    }

    /**
     * Lấy chi tiết thông báo.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ThongBao> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Tạo thông báo hệ thống (Admin).
     */
    @PostMapping("/thong-bao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThongBao> createThongBao(@RequestBody Map<String, String> request) {
        ThongBao thongBao = service.createThongBao(
            request.get("tieuDe"),
            request.get("noiDung"),
            request.get("nguoiTao"),
            request.get("loaiThongBao")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(thongBao);
    }

    /**
     * Tạo thông báo cho tòa nhà (Manager).
     */
    @PostMapping("/toa-nha/{toaNhaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ThongBao> createForBuilding(
            @PathVariable Integer toaNhaId,
            @RequestBody Map<String, String> request) {
        ThongBao thongBao = service.createForBuilding(
            toaNhaId,
            request.get("tieuDe"),
            request.get("noiDung"),
            request.getOrDefault("loaiThongBao", "Thông báo chung")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(thongBao);
    }

    /**
     * Xóa thông báo.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        service.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã xóa thông báo");
        return ResponseEntity.ok(response);
    }

    // ===== Gửi thông báo nhắc hạn =====

    @PostMapping("/nhac-han")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> sendPaymentReminder(
            @RequestParam @NonNull Integer toaNhaId,
            @RequestParam @NonNull String tenDotThu) {
        service.sendPaymentReminderToBuilding(toaNhaId, tenDotThu);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã gửi thông báo nhắc hạn cho tòa nhà");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/gui-hoa-don/{idHoaDon}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> sendInvoiceByEmail(@PathVariable @NonNull Integer idHoaDon) {
        service.sendInvoiceByEmail(idHoaDon);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã gửi hóa đơn qua email");
        return ResponseEntity.ok(response);
    }
}

