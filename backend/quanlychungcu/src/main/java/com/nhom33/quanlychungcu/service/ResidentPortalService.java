package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.HoaDon;
import com.nhom33.quanlychungcu.entity.LichSuThanhToan;
import com.nhom33.quanlychungcu.entity.PhanAnh;
import com.nhom33.quanlychungcu.repository.HoaDonRepository;
import com.nhom33.quanlychungcu.repository.PhanAnhRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service dành cho cư dân xem thông tin của mình
 */
@Service
public class ResidentPortalService {

    private final HoaDonRepository hoaDonRepo;
    private final PhanAnhRepository phanAnhRepo;

    public ResidentPortalService(HoaDonRepository hoaDonRepo, PhanAnhRepository phanAnhRepo) {
        this.hoaDonRepo = hoaDonRepo;
        this.phanAnhRepo = phanAnhRepo;
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
     * Lấy danh sách phản ánh của hộ gia đình
     */
    public Page<PhanAnh> getPhanAnhByHoGiaDinh(@NonNull Integer idHoGiaDinh, @NonNull Pageable pageable) {
        return phanAnhRepo.findByHoGiaDinhId(idHoGiaDinh, pageable);
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

