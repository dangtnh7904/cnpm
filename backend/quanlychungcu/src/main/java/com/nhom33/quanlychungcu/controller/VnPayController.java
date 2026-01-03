package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.service.VnPayService;
import com.nhom33.quanlychungcu.service.VnPayService.VnPayResult;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý thanh toán VNPAY.
 * 
 * ENDPOINTS:
 * - POST /api/payment/vnpay/create/{hoaDonId}: Tạo URL thanh toán
 * - GET  /api/payment/vnpay-return: VNPAY callback (redirect user về đây sau khi thanh toán)
 */
@RestController
@RequestMapping("/api/payment")
public class VnPayController {

    private static final Logger log = LoggerFactory.getLogger(VnPayController.class);

    private final VnPayService vnPayService;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public VnPayController(VnPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    /**
     * API 1: Tạo URL thanh toán VNPAY.
     * 
     * Frontend gọi API này để lấy URL, sau đó redirect user sang VNPAY.
     * 
     * @param hoaDonId ID hóa đơn cần thanh toán
     * @param amount Số tiền thanh toán (tùy chọn, mặc định = số tiền còn nợ)
     * @param request HttpServletRequest
     * @return JSON chứa paymentUrl
     */
    @PostMapping("/vnpay/create/{hoaDonId}")
    public ResponseEntity<Map<String, String>> createPaymentUrl(
            @PathVariable Integer hoaDonId,
            @RequestParam(required = false) Long amount,
            HttpServletRequest request) {

        log.info("Creating VNPAY payment URL for HoaDon {}, amount: {}", hoaDonId, amount);

        // Nếu không truyền amount, service sẽ lấy số tiền còn nợ
        if (amount == null) {
            amount = 0L; // Service sẽ validate và dùng số tiền còn nợ
        }

        String orderInfo = "Thanh toan hoa don " + hoaDonId;
        String paymentUrl = vnPayService.createPaymentUrl(hoaDonId, amount, orderInfo, request);

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        response.put("hoaDonId", String.valueOf(hoaDonId));

        return ResponseEntity.ok(response);
    }

    /**
     * API 2: VNPAY Return URL.
     * 
     * VNPAY sẽ redirect user về URL này sau khi thanh toán.
     * Backend xử lý kết quả và redirect user sang Frontend.
     * 
     * QUAN TRỌNG:
     * - Validate checksum trước khi xử lý
     * - Chỉ cập nhật DB khi ResponseCode = "00"
     * - Redirect về Frontend với status tương ứng
     */
    @GetMapping("/vnpay-return")
    public void handleVnPayReturn(HttpServletRequest request, 
                                  jakarta.servlet.http.HttpServletResponse response) {
        try {
            // Xử lý kết quả thanh toán
            VnPayResult result = vnPayService.processPaymentReturn(request);

            // Build redirect URL cho Frontend
            String redirectUrl;
            if (result.isSuccess()) {
                // THÀNH CÔNG
                redirectUrl = String.format("%s/payment-result?status=success&hoaDonId=%d&message=%s",
                    frontendUrl,
                    result.getHoaDonId(),
                    URLEncoder.encode(result.getMessage(), StandardCharsets.UTF_8));
            } else {
                // THẤT BẠI
                redirectUrl = String.format("%s/payment-result?status=failed&code=%s&message=%s%s",
                    frontendUrl,
                    result.getResponseCode(),
                    URLEncoder.encode(result.getMessage(), StandardCharsets.UTF_8),
                    result.getHoaDonId() != null ? "&hoaDonId=" + result.getHoaDonId() : "");
            }

            log.info("Redirecting to Frontend: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing VNPAY return", e);
            try {
                String errorUrl = String.format("%s/payment-result?status=error&message=%s",
                    frontendUrl,
                    URLEncoder.encode("Có lỗi xảy ra khi xử lý thanh toán: " + e.getMessage(), StandardCharsets.UTF_8));
                response.sendRedirect(errorUrl);
            } catch (Exception ex) {
                log.error("Failed to redirect to error page", ex);
            }
        }
    }

    /**
     * API kiểm tra trạng thái thanh toán (Optional).
     * Frontend có thể gọi để verify sau khi thanh toán.
     */
    @GetMapping("/vnpay/status/{hoaDonId}")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable Integer hoaDonId) {
        // TODO: Implement nếu cần query VNPAY API để check status
        Map<String, Object> response = new HashMap<>();
        response.put("hoaDonId", hoaDonId);
        response.put("message", "Use invoice API to check payment status");
        return ResponseEntity.ok(response);
    }
}
