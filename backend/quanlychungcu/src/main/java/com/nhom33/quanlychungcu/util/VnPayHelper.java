package com.nhom33.quanlychungcu.util;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Helper class cho VNPAY Integration.
 * 
 * Xử lý:
 * - HMAC SHA512 hash
 * - Build URL thanh toán
 * - Lấy IP Address client
 * - Validate checksum
 */
public class VnPayHelper {

    /**
     * Hash HMAC SHA512.
     * Đây là thuật toán mã hóa bắt buộc của VNPAY.
     */
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new IllegalArgumentException("Key và Data không được null");
            }
            
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Lỗi mã hóa HMAC SHA512", e);
        }
    }

    /**
     * Lấy địa chỉ IP của client.
     * Hỗ trợ các trường hợp: Proxy, Load Balancer, Direct connection.
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            // Thử các header theo thứ tự ưu tiên
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("X-Real-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            
            // Nếu có nhiều IP (qua nhiều proxy), lấy IP đầu tiên
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }
            
            // Handle localhost IPv6
            if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
            
        } catch (Exception e) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }

    /**
     * Tạo ngày giờ theo format VNPAY yêu cầu: yyyyMMddHHmmss
     */
    public static String getVnpCreateDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return formatter.format(new Date());
    }

    /**
     * Tạo thời gian hết hạn (mặc định 15 phút).
     */
    public static String getVnpExpireDate() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        cal.add(Calendar.MINUTE, 15);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return formatter.format(cal.getTime());
    }

    /**
     * Tạo mã giao dịch duy nhất (TxnRef).
     * Format: HoaDonId + Timestamp
     */
    public static String generateTxnRef(Integer hoaDonId) {
        return hoaDonId + "_" + System.currentTimeMillis();
    }

    /**
     * Build URL thanh toán VNPAY.
     * 
     * QUAN TRỌNG:
     * - Các tham số phải được sắp xếp theo thứ tự ABC
     * - Hash được tính từ query string (không bao gồm vnp_SecureHash)
     */
    public static String buildPaymentUrl(String baseUrl, Map<String, String> params, String hashSecret) {
        // Sắp xếp params theo thứ tự ABC
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName).append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    hashData.append(fieldValue);
                }

                // Build query string
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        // Tính SecureHash
        String secureHash = hmacSHA512(hashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return baseUrl + "?" + query.toString();
    }

    /**
     * Validate SecureHash từ VNPAY trả về.
     * 
     * CRITICAL: Phải validate để chống giả mạo request!
     */
    public static boolean validateChecksum(Map<String, String> params, String hashSecret) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
            return false;
        }

        // Loại bỏ các field hash khỏi params để tính lại
        Map<String, String> paramsToHash = new HashMap<>(params);
        paramsToHash.remove("vnp_SecureHash");
        paramsToHash.remove("vnp_SecureHashType");

        // Sắp xếp và build hash data
        List<String> fieldNames = new ArrayList<>(paramsToHash.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = paramsToHash.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    hashData.append(fieldValue);
                }
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }
        }

        // Tính lại hash và so sánh
        String calculatedHash = hmacSHA512(hashSecret, hashData.toString());
        return vnpSecureHash.equalsIgnoreCase(calculatedHash);
    }

    /**
     * Lấy mô tả lỗi từ Response Code.
     */
    public static String getResponseMessage(String responseCode) {
        return switch (responseCode) {
            case "00" -> "Giao dịch thành công";
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo)";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký InternetBanking";
            case "10" -> "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11" -> "Đã hết hạn chờ thanh toán. Vui lòng thực hiện lại giao dịch";
            case "12" -> "Thẻ/Tài khoản bị khóa";
            case "13" -> "Nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24" -> "Khách hàng hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư để thực hiện giao dịch";
            case "65" -> "Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99" -> "Lỗi không xác định";
            default -> "Lỗi giao dịch: " + responseCode;
        };
    }
}
