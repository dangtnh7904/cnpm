package com.nhom33.quanlychungcu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình VNPAY Sandbox.
 * 
 * CÁC BƯỚC LẤY THÔNG TIN:
 * 1. Đăng ký Sandbox tại: https://sandbox.vnpayment.vn/
 * 2. Lấy TmnCode và HashSecret từ email VNPAY gửi
 * 3. Điền vào application.properties
 * 
 * VNPAY Response Codes:
 * - 00: Giao dịch thành công
 * - 07: Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)
 * - 09: Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking
 * - 10: Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần
 * - 11: Đã hết hạn chờ thanh toán
 * - 12: Thẻ/Tài khoản bị khóa
 * - 13: Nhập sai mật khẩu xác thực giao dịch (OTP)
 * - 24: Khách hàng hủy giao dịch
 * - 51: Tài khoản không đủ số dư
 * - 65: Tài khoản đã vượt quá hạn mức giao dịch trong ngày
 * - 75: Ngân hàng thanh toán đang bảo trì
 * - 79: Nhập sai mật khẩu thanh toán quá số lần quy định
 * - 99: Các lỗi khác
 */
@Configuration
public class VnPayConfig {

    // === VNPAY Sandbox URLs ===
    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url:http://localhost:8080/api/payment/vnpay-return}")
    private String vnpReturnUrl;

    @Value("${vnpay.api-url:https://sandbox.vnpayment.vn/merchant_webapi/api/transaction}")
    private String vnpApiUrl;

    // === VNPAY Credentials (LẤY TỪ SANDBOX) ===
    @Value("${vnpay.tmn-code:YOUR_TMN_CODE}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:YOUR_HASH_SECRET}")
    private String vnpHashSecret;

    // === VNPAY Constants ===
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    public static final String VNP_CURR_CODE = "VND";
    public static final String VNP_LOCALE = "vn";
    public static final String VNP_ORDER_TYPE = "other";

    // Getters
    public String getVnpPayUrl() {
        return vnpPayUrl;
    }

    public String getVnpReturnUrl() {
        return vnpReturnUrl;
    }

    public String getVnpApiUrl() {
        return vnpApiUrl;
    }

    public String getVnpTmnCode() {
        return vnpTmnCode;
    }

    public String getVnpHashSecret() {
        return vnpHashSecret;
    }
}
