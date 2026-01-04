package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.HoaDon;
import com.nhom33.quanlychungcu.entity.LichSuThanhToan;
import com.nhom33.quanlychungcu.entity.PhanAnh;
import com.nhom33.quanlychungcu.entity.UserAccount;
import com.nhom33.quanlychungcu.repository.HoaDonRepository;
import com.nhom33.quanlychungcu.repository.PhanAnhRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service dành cho cư dân (RESIDENT) xem thông tin của mình.
 * 
 * LOGIC MỚI:
 * - Phản ánh: Lấy theo user ID (không theo hộ gia đình)
 * - Hóa đơn: Lấy theo hộ gia đình mà user chọn thanh toán
 */
@Service
public class ResidentPortalService {

    private final HoaDonRepository hoaDonRepo;
    private final PhanAnhRepository phanAnhRepo;
    private final SecurityHelper securityHelper;

    public ResidentPortalService(HoaDonRepository hoaDonRepo, 
                                  PhanAnhRepository phanAnhRepo,
                                  SecurityHelper securityHelper) {
        this.hoaDonRepo = hoaDonRepo;
        this.phanAnhRepo = phanAnhRepo;
        this.securityHelper = securityHelper;
    }

    /**
     * Lấy lịch sử thanh toán của hộ gia đình
     */
    public Page<HoaDon> getPaymentHistory(@NonNull Integer idHoGiaDinh, @NonNull Pageable pageable) {
        return hoaDonRepo.findByHoGiaDinhId(idHoGiaDinh, pageable);
    }

    /**
     * Lấy chi tiết lịch sử thanh toán của một hóa đơn
     */
    public List<LichSuThanhToan> getPaymentDetails(@NonNull Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        
        return hoaDon.getDanhSachThanhToan();
    }

    /**
     * Lấy danh sách phản ánh của user hiện tại.
     */
    public Page<PhanAnh> getMyPhanAnh(@NonNull Pageable pageable) {
        UserAccount user = securityHelper.getCurrentUser();
        if (user == null) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        return phanAnhRepo.findByUserId(user.getId(), pageable);
    }

    /**
     * Lấy danh sách phản ánh của hộ gia đình.
     */
    public Page<PhanAnh> getPhanAnhByHoGiaDinh(@NonNull Integer idHoGiaDinh, @NonNull Pageable pageable) {
        // Phản ánh liên quan tới hộ gia đình - có thể mở rộng logic sau
        // Hiện tại trả về rỗng vì PhanAnh không liên kết trực tiếp với HoGiaDinh
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    /**
     * Lấy công nợ hiện tại của hộ gia đình
     */
    public java.math.BigDecimal getCurrentDebt(@NonNull Integer idHoGiaDinh) {
        List<HoaDon> hoaDons = hoaDonRepo.findByHoGiaDinhId(idHoGiaDinh);
        java.math.BigDecimal tongCongNo = java.math.BigDecimal.ZERO;
        
        for (HoaDon hoaDon : hoaDons) {
            java.math.BigDecimal conNo = hoaDon.getSoTienConNo();
            if (conNo.compareTo(java.math.BigDecimal.ZERO) > 0) {
                tongCongNo = tongCongNo.add(conNo);
            }
        }
        
        return tongCongNo;
    }
}

