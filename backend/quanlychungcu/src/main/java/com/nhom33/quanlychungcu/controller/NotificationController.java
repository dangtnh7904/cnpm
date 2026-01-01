package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.ThongBao;
import com.nhom33.quanlychungcu.service.NotificationService;
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

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

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

    @PostMapping("/nhac-han/{idHoaDon}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendPaymentReminder(@PathVariable @NonNull Integer idHoaDon) {
        service.sendPaymentReminder(idHoaDon);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã gửi thông báo nhắc hạn");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/nhac-han-hang-loat/{idDotThu}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendBulkPaymentReminder(@PathVariable @NonNull Integer idDotThu) {
        int sentCount = service.sendBulkPaymentReminder(idDotThu);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đã gửi thông báo");
        response.put("sentCount", sentCount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/gui-hoa-don/{idHoaDon}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendInvoiceByEmail(@PathVariable @NonNull Integer idHoaDon) {
        service.sendInvoiceByEmail(idHoaDon);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã gửi hóa đơn qua email");
        return ResponseEntity.ok(response);
    }
}

