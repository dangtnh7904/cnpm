package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.service.PaymentService;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý thanh toán cơ bản.
 * 
 * LƯU Ý: Các endpoint VNPAY đã được chuyển sang VnPayController.java
 * - POST /api/payment/vnpay/create/{id} -> VnPayController
 * - GET  /api/payment/vnpay-return -> VnPayController
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    // Các endpoint VNPAY đã được chuyển sang VnPayController
    // Để tránh xung đột "Ambiguous handler methods"
}

