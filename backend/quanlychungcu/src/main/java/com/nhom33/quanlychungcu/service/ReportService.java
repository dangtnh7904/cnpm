package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.repository.HoaDonRepository;
import com.nhom33.quanlychungcu.repository.DotThuRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private final HoaDonRepository hoaDonRepo;
    private final DotThuRepository dotThuRepo;
    private final com.nhom33.quanlychungcu.repository.HoGiaDinhRepository hoGiaDinhRepo;

    public ReportService(HoaDonRepository hoaDonRepo, DotThuRepository dotThuRepo, com.nhom33.quanlychungcu.repository.HoGiaDinhRepository hoGiaDinhRepo) {
        this.hoaDonRepo = hoaDonRepo;
        this.dotThuRepo = dotThuRepo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
    }

    /**
     * Thống kê theo đợt thu
     */
    public Map<String, Object> getStatisticsByDotThu(Integer idDotThu) {
        BigDecimal tongPhaiThu = hoaDonRepo.sumTongTienPhaiThuByDotThu(idDotThu);
        BigDecimal tongDaThu = hoaDonRepo.sumSoTienDaDongByDotThu(idDotThu);
        
        if (tongPhaiThu == null) tongPhaiThu = BigDecimal.ZERO;
        if (tongDaThu == null) tongDaThu = BigDecimal.ZERO;
        
        BigDecimal tongConNo = tongPhaiThu.subtract(tongDaThu);
        BigDecimal tyLeHoanThanh = tongPhaiThu.compareTo(BigDecimal.ZERO) > 0
            ? tongDaThu.multiply(BigDecimal.valueOf(100)).divide(tongPhaiThu, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        Long soHoChuaDong = hoaDonRepo.countByDotThuAndTrangThai(idDotThu, "Chưa đóng");
        Long soHoDangNo = hoaDonRepo.countByDotThuAndTrangThai(idDotThu, "Đang nợ");
        Long soHoDaDong = hoaDonRepo.countByDotThuAndTrangThai(idDotThu, "Đã đóng");
        
        // Fix: Lấy tổng số hộ thực tế từ DB thay vì cộng các hóa đơn
        long tongSoHo = hoGiaDinhRepo.count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("tongPhaiThu", tongPhaiThu);
        result.put("tongDaThu", tongDaThu);
        result.put("tongConNo", tongConNo);
        result.put("tyLeHoanThanh", tyLeHoanThanh);
        result.put("soHoChuaDong", soHoChuaDong);
        result.put("soHoDangNo", soHoDangNo);
        result.put("soHoDaDong", soHoDaDong);
        result.put("tongSoHo", tongSoHo);
        
        return result;
    }

    /**
     * Thống kê công nợ theo hộ gia đình
     */
    public Map<String, Object> getCongNoByHoGiaDinh(Integer idHoGiaDinh) {
        // Lấy tất cả hóa đơn chưa đóng đủ của hộ
        var hoaDons = hoaDonRepo.findByHoGiaDinhId(idHoGiaDinh);
        
        BigDecimal tongCongNo = BigDecimal.ZERO;
        int soHoaDonChuaDong = 0;
        
        for (var hoaDon : hoaDons) {
            BigDecimal conNo = hoaDon.getSoTienConNo();
            if (conNo.compareTo(BigDecimal.ZERO) > 0) {
                tongCongNo = tongCongNo.add(conNo);
                soHoaDonChuaDong++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("tongCongNo", tongCongNo);
        result.put("soHoaDonChuaDong", soHoaDonChuaDong);
        
        return result;
    }

    /**
     * Thống kê tổng hợp theo tháng
     */
    public Map<String, Object> getStatisticsByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        // Lấy các đợt thu trong tháng
        var dotThus = dotThuRepo.findByNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqual(endDate, startDate);
        
        BigDecimal tongPhaiThu = BigDecimal.ZERO;
        BigDecimal tongDaThu = BigDecimal.ZERO;
        
        for (var dotThu : dotThus) {
            BigDecimal phaiThu = hoaDonRepo.sumTongTienPhaiThuByDotThu(dotThu.getId());
            BigDecimal daThu = hoaDonRepo.sumSoTienDaDongByDotThu(dotThu.getId());
            
            if (phaiThu != null) tongPhaiThu = tongPhaiThu.add(phaiThu);
            if (daThu != null) tongDaThu = tongDaThu.add(daThu);
        }
        
        BigDecimal tongConNo = tongPhaiThu.subtract(tongDaThu);
        BigDecimal tyLeHoanThanh = tongPhaiThu.compareTo(BigDecimal.ZERO) > 0
            ? tongDaThu.multiply(BigDecimal.valueOf(100)).divide(tongPhaiThu, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        Map<String, Object> result = new HashMap<>();
        result.put("thang", month);
        result.put("nam", year);
        result.put("tongPhaiThu", tongPhaiThu);
        result.put("tongDaThu", tongDaThu);
        result.put("tongConNo", tongConNo);
        result.put("tyLeHoanThanh", tyLeHoanThanh);
        
        return result;
    }
}

