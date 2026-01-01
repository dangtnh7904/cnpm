package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.HoaDon;
import com.nhom33.quanlychungcu.entity.LichSuThanhToan;
import com.nhom33.quanlychungcu.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentService {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonService hoaDonService;

    @Value("${vnpay.tmn-code:}")
    private String vnpayTmnCode;

    @Value("${vnpay.hash-secret:}")
    private String vnpayHashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpayUrl;

    @Value("${vnpay.return-url:http://localhost:3000/payment/callback}")
    private String vnpayReturnUrl;

    public PaymentService(HoaDonRepository hoaDonRepo, HoaDonService hoaDonService) {
        this.hoaDonRepo = hoaDonRepo;
        this.hoaDonService = hoaDonService;
    }

    /**
     * Tạo URL thanh toán VNPay
     */
    public String createPaymentUrl(Integer idHoaDon, String ipAddress) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        BigDecimal amount = hoaDon.getSoTienConNo();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hóa đơn đã được thanh toán đủ");
        }

        // Chuyển đổi sang VNĐ (nhân 100)
        long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpayTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", String.valueOf(idHoaDon));
        vnpParams.put("vnp_OrderInfo", "Thanh toan hoa don #" + idHoaDon);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpayReturnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        // Sắp xếp và tạo query string
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                     .append('=')
                     .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String vnpSecureHash = hmacSHA512(vnpayHashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);

        return vnpayUrl + "?" + query.toString();
    }

    /**
     * Xử lý callback từ VNPay
     */
    @Transactional
    public LichSuThanhToan processPaymentCallback(Map<String, String> params) {
        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTxnRef = params.get("vnp_TxnRef");
        String vnpAmount = params.get("vnp_Amount");
        String vnpTransactionStatus = params.get("vnp_TransactionStatus");

        Integer idHoaDon = Integer.parseInt(vnpTxnRef);
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Kiểm tra chữ ký
        if (!verifySignature(params)) {
            throw new RuntimeException("Chữ ký không hợp lệ");
        }

        // Kiểm tra response code
        if (!"00".equals(vnpResponseCode) || !"00".equals(vnpTransactionStatus)) {
            throw new RuntimeException("Giao dịch thất bại. Mã lỗi: " + vnpResponseCode);
        }

        // Chuyển đổi số tiền (chia 100)
        BigDecimal amount = BigDecimal.valueOf(Long.parseLong(vnpAmount)).divide(BigDecimal.valueOf(100));

        // Thêm vào lịch sử thanh toán
        return hoaDonService.addPayment(idHoaDon, amount, "VNPay", hoaDon.getHoGiaDinh().getTenChuHo(), 
            "Thanh toán qua VNPay - Mã giao dịch: " + params.get("vnp_TransactionNo"));
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] hash = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký", e);
        }
    }

    private boolean verifySignature(Map<String, String> params) {
        String vnpSecureHash = params.remove("vnp_SecureHash");
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty() && !fieldName.equals("vnp_SecureHash")) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }
        }
        
        String calculatedHash = hmacSHA512(vnpayHashSecret, hashData.toString());
        return calculatedHash.equals(vnpSecureHash);
    }
}

