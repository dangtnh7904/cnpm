package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.config.VnPayConfig;
import com.nhom33.quanlychungcu.entity.HoaDon;
import com.nhom33.quanlychungcu.entity.LichSuThanhToan;
import com.nhom33.quanlychungcu.exception.BadRequestException;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.HoaDonRepository;
import com.nhom33.quanlychungcu.repository.LichSuThanhToanRepository;
import com.nhom33.quanlychungcu.util.VnPayHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service xử lý thanh toán VNPAY.
 * 
 * LUỒNG THANH TOÁN:
 * 1. Frontend gọi createPaymentUrl() để lấy URL redirect sang VNPAY
 * 2. User thanh toán trên trang VNPAY
 * 3. VNPAY redirect về vnp_ReturnUrl với kết quả
 * 4. Backend xử lý kết quả: validate checksum -> cập nhật DB -> redirect frontend
 */
@Service
public class VnPayService {

    private static final Logger log = LoggerFactory.getLogger(VnPayService.class);

    private final VnPayConfig vnPayConfig;
    private final HoaDonRepository hoaDonRepo;
    private final LichSuThanhToanRepository lichSuRepo;

    public VnPayService(VnPayConfig vnPayConfig, 
                        HoaDonRepository hoaDonRepo,
                        LichSuThanhToanRepository lichSuRepo) {
        this.vnPayConfig = vnPayConfig;
        this.hoaDonRepo = hoaDonRepo;
        this.lichSuRepo = lichSuRepo;
    }

    /**
     * Tạo URL thanh toán VNPAY.
     * 
     * @param hoaDonId ID hóa đơn cần thanh toán
     * @param amount Số tiền thanh toán (VND). Nếu = 0, lấy số tiền còn nợ.
     * @param orderInfo Nội dung thanh toán
     * @param request HttpServletRequest để lấy IP
     * @return URL redirect sang VNPAY
     */
    public String createPaymentUrl(Integer hoaDonId, Long amount, String orderInfo, HttpServletRequest request) {
        // Validate
        HoaDon hoaDon = hoaDonRepo.findById(hoaDonId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + hoaDonId));

        // Kiểm tra đợt thu đã bắt đầu chưa
        if (hoaDon.getDotThu() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate ngayBatDau = hoaDon.getDotThu().getNgayBatDau();
            java.time.LocalDate ngayKetThuc = hoaDon.getDotThu().getNgayKetThuc();
            
            if (ngayBatDau != null && today.isBefore(ngayBatDau)) {
                throw new BadRequestException("Đợt thu chưa bắt đầu. Ngày bắt đầu: " + ngayBatDau);
            }
            if (ngayKetThuc != null && today.isAfter(ngayKetThuc)) {
                throw new BadRequestException("Đợt thu đã kết thúc. Ngày kết thúc: " + ngayKetThuc);
            }
        }

        // Tính số tiền còn nợ
        BigDecimal conNo = hoaDon.getTongTienPhaiThu().subtract(hoaDon.getSoTienDaDong());
        
        // Nếu không truyền amount hoặc = 0, lấy số tiền còn nợ
        if (amount == null || amount <= 0) {
            amount = conNo.longValue();
        }
        
        // Validate số tiền
        if (amount <= 0) {
            throw new BadRequestException("Hóa đơn đã được thanh toán đầy đủ");
        }
        if (BigDecimal.valueOf(amount).compareTo(conNo) > 0) {
            throw new BadRequestException("Số tiền thanh toán không được lớn hơn số tiền còn nợ: " + conNo);
        }

        // Build params theo spec VNPAY
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", VnPayConfig.VNP_VERSION);
        vnpParams.put("vnp_Command", VnPayConfig.VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPAY yêu cầu nhân 100
        vnpParams.put("vnp_CurrCode", VnPayConfig.VNP_CURR_CODE);
        vnpParams.put("vnp_TxnRef", VnPayHelper.generateTxnRef(hoaDonId));
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", VnPayConfig.VNP_ORDER_TYPE);
        vnpParams.put("vnp_Locale", VnPayConfig.VNP_LOCALE);
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnpParams.put("vnp_IpAddr", VnPayHelper.getIpAddress(request));
        vnpParams.put("vnp_CreateDate", VnPayHelper.getVnpCreateDate());
        vnpParams.put("vnp_ExpireDate", VnPayHelper.getVnpExpireDate());

        String paymentUrl = VnPayHelper.buildPaymentUrl(
            vnPayConfig.getVnpPayUrl(), 
            vnpParams, 
            vnPayConfig.getVnpHashSecret()
        );

        log.info("Created VNPAY payment URL for HoaDon {}, Amount: {}", hoaDonId, amount);
        return paymentUrl;
    }

    /**
     * Xử lý kết quả trả về từ VNPAY.
     * 
     * CRITICAL: Phải validate checksum trước khi xử lý!
     * 
     * @param request HttpServletRequest chứa params từ VNPAY
     * @return VnPayResult chứa thông tin kết quả
     */
    @Transactional
    public VnPayResult processPaymentReturn(HttpServletRequest request) {
        // Extract tất cả params từ request
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        log.info("Processing VNPAY return: {}", params);

        // === BƯỚC 1: Validate Checksum ===
        boolean isValidChecksum = VnPayHelper.validateChecksum(params, vnPayConfig.getVnpHashSecret());
        if (!isValidChecksum) {
            log.error("VNPAY Checksum validation FAILED! Possible security breach.");
            return new VnPayResult(false, "CHECKSUM_FAILED", "Chữ ký không hợp lệ. Giao dịch bị từ chối vì lý do bảo mật.", null);
        }

        // Extract thông tin cần thiết
        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTxnRef = params.get("vnp_TxnRef"); // Format: hoaDonId_timestamp
        String vnpAmount = params.get("vnp_Amount");
        String vnpTransactionNo = params.get("vnp_TransactionNo");
        String vnpBankCode = params.get("vnp_BankCode");
        String vnpOrderInfo = params.get("vnp_OrderInfo");

        // Parse hoaDonId từ TxnRef
        Integer hoaDonId;
        try {
            hoaDonId = Integer.parseInt(vnpTxnRef.split("_")[0]);
        } catch (Exception e) {
            log.error("Invalid TxnRef format: {}", vnpTxnRef);
            return new VnPayResult(false, "INVALID_TXNREF", "Mã giao dịch không hợp lệ", null);
        }

        // Tính số tiền (VNPAY trả về đã nhân 100)
        BigDecimal amount = new BigDecimal(vnpAmount).divide(BigDecimal.valueOf(100));

        // === BƯỚC 2: Kiểm tra Response Code ===
        if (!"00".equals(vnpResponseCode)) {
            // GIAO DỊCH THẤT BẠI - KHÔNG LƯU, KHÔNG CẬP NHẬT HÓA ĐƠN
            String errorMessage = VnPayHelper.getResponseMessage(vnpResponseCode);
            log.warn("VNPAY transaction FAILED for HoaDon {}: {} - {}", hoaDonId, vnpResponseCode, errorMessage);
            
            return new VnPayResult(false, vnpResponseCode, errorMessage, hoaDonId);
        }

        // === BƯỚC 3: GIAO DỊCH THÀNH CÔNG - Cập nhật Database ===
        HoaDon hoaDon = hoaDonRepo.findById(hoaDonId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + hoaDonId));

        // Kiểm tra duplicate (tránh xử lý lại nếu user F5)
        boolean isDuplicate = lichSuRepo.existsByMaGiaoDichVnpay(vnpTransactionNo);
        if (isDuplicate) {
            log.warn("Duplicate VNPAY transaction detected: {}", vnpTransactionNo);
            return new VnPayResult(true, "00", "Giao dịch đã được xử lý trước đó", hoaDonId);
        }

        // 3a. Lưu lịch sử thanh toán
        LichSuThanhToan lichSu = new LichSuThanhToan();
        lichSu.setHoaDon(hoaDon);
        lichSu.setSoTien(amount);
        lichSu.setHinhThuc("VNPAY");
        lichSu.setNgayNop(LocalDateTime.now());
        lichSu.setGhiChu(vnpOrderInfo);
        lichSu.setMaGiaoDichVnpay(vnpTransactionNo);
        lichSu.setMaNganHang(vnpBankCode);
        lichSu.setMaPhanHoi(vnpResponseCode);
        lichSuRepo.save(lichSu);

        // 3b. Cập nhật hóa đơn
        BigDecimal soTienDaDongMoi = hoaDon.getSoTienDaDong().add(amount);
        hoaDon.setSoTienDaDong(soTienDaDongMoi);

        // Cập nhật trạng thái
        if (soTienDaDongMoi.compareTo(hoaDon.getTongTienPhaiThu()) >= 0) {
            hoaDon.setTrangThai("DaThanhToan");
        } else if (soTienDaDongMoi.compareTo(BigDecimal.ZERO) > 0) {
            hoaDon.setTrangThai("ThanhToanMotPhan");
        }
        hoaDonRepo.save(hoaDon);

        log.info("VNPAY transaction SUCCESS for HoaDon {}: {} VND via {}", hoaDonId, amount, vnpBankCode);

        return new VnPayResult(true, "00", "Thanh toán thành công", hoaDonId);
    }

    /**
     * DTO chứa kết quả xử lý VNPAY.
     */
    public static class VnPayResult {
        private final boolean success;
        private final String responseCode;
        private final String message;
        private final Integer hoaDonId;

        public VnPayResult(boolean success, String responseCode, String message, Integer hoaDonId) {
            this.success = success;
            this.responseCode = responseCode;
            this.message = message;
            this.hoaDonId = hoaDonId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getResponseCode() {
            return responseCode;
        }

        public String getMessage() {
            return message;
        }

        public Integer getHoaDonId() {
            return hoaDonId;
        }
    }
}
