package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.LichSuThanhToan;
import com.nhom33.quanlychungcu.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/vnpay/create/{idHoaDon}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, String>> createVnPayUrl(
            @PathVariable @NonNull Integer idHoaDon,
            HttpServletRequest request) {
        
        String ipAddress = getClientIpAddress(request);
        String paymentUrl = service.createPaymentUrl(idHoaDon, ipAddress);
        
        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vnpay/callback")
    public ResponseEntity<Map<String, Object>> vnpayCallback(
            @RequestParam Map<String, String> params) {
        
        try {
            LichSuThanhToan thanhToan = service.processPaymentCallback(params);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thanh toán thành công");
            response.put("transactionId", thanhToan.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

