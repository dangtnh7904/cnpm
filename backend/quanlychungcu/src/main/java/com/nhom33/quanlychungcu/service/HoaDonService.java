package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.*;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HoaDonService {

    private final HoaDonRepository hoaDonRepo;
    private final HoGiaDinhRepository hoGiaDinhRepo;
    private final DotThuRepository dotThuRepo;
    private final DinhMucThuRepository dinhMucRepo;
    private final ChiTietHoaDonRepository chiTietRepo;
    private final LichSuThanhToanRepository thanhToanRepo;
    private final BangGiaService bangGiaService;

    public HoaDonService(HoaDonRepository hoaDonRepo,
                        HoGiaDinhRepository hoGiaDinhRepo,
                        DotThuRepository dotThuRepo,
                        DinhMucThuRepository dinhMucRepo,
                        ChiTietHoaDonRepository chiTietRepo,
                        LichSuThanhToanRepository thanhToanRepo,
                        BangGiaService bangGiaService) {
        this.hoaDonRepo = hoaDonRepo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
        this.dotThuRepo = dotThuRepo;
        this.dinhMucRepo = dinhMucRepo;
        this.chiTietRepo = chiTietRepo;
        this.thanhToanRepo = thanhToanRepo;
        this.bangGiaService = bangGiaService;
    }

    @Transactional
    public HoaDon createHoaDonForHoGiaDinh(Integer idHoGiaDinh, Integer idDotThu) {
        HoGiaDinh hoGiaDinh = hoGiaDinhRepo.findById(idHoGiaDinh)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + idHoGiaDinh));
        
        DotThu dotThu = dotThuRepo.findById(idDotThu)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + idDotThu));

        // Kiểm tra đã có hóa đơn chưa
        hoaDonRepo.findByHoGiaDinhIdAndDotThuId(idHoGiaDinh, idDotThu)
            .ifPresent(hd -> {
                throw new IllegalArgumentException("Hộ gia đình đã có hóa đơn cho đợt thu này");
            });

        // Lấy định mức thu của hộ
        List<DinhMucThu> dinhMucList = dinhMucRepo.findActiveByHoGiaDinhId(idHoGiaDinh);
        
        HoaDon hoaDon = new HoaDon();
        hoaDon.setHoGiaDinh(hoGiaDinh);
        hoaDon.setDotThu(dotThu);
        hoaDon.setTongTienPhaiThu(BigDecimal.ZERO);
        hoaDon.setSoTienDaDong(BigDecimal.ZERO);
        hoaDon.setTrangThai("Chưa đóng");
        
        hoaDon = hoaDonRepo.save(hoaDon);

        // Tạo chi tiết hóa đơn từ định mức
        // Lấy ID tòa nhà để áp dụng giá ưu tiên
        Integer toaNhaId = hoGiaDinh.getToaNha() != null ? hoGiaDinh.getToaNha().getId() : null;
        
        BigDecimal tongTien = BigDecimal.ZERO;
        for (DinhMucThu dm : dinhMucList) {
            LoaiPhi loaiPhi = dm.getLoaiPhi();
            if (loaiPhi.getDangHoatDong()) {
                // Lấy giá ưu tiên: BangGiaDichVu (theo tòa) > LoaiPhi.donGia (mặc định)
                BigDecimal donGia = bangGiaService.getDonGiaApDung(loaiPhi.getId(), toaNhaId);
                
                ChiTietHoaDon chiTiet = new ChiTietHoaDon();
                chiTiet.setHoaDon(hoaDon);
                chiTiet.setLoaiPhi(loaiPhi);
                chiTiet.setSoLuong(dm.getSoLuong());
                chiTiet.setDonGia(donGia);
                chiTiet.setThanhTien(donGia.multiply(BigDecimal.valueOf(dm.getSoLuong())));
                
                chiTietRepo.save(chiTiet);
                tongTien = tongTien.add(chiTiet.getThanhTien());
            }
        }

        hoaDon.setTongTienPhaiThu(tongTien);
        return hoaDonRepo.save(hoaDon);
    }

    @Transactional
    public HoaDon updateTrangThai(Integer id, String trangThai) {
        HoaDon hoaDon = hoaDonRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + id));
        
        hoaDon.setTrangThai(trangThai);
        return hoaDonRepo.save(hoaDon);
    }

    @Transactional
    public LichSuThanhToan addPayment(Integer idHoaDon, BigDecimal soTien, String hinhThuc, String nguoiNop, String ghiChu) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + idHoaDon));

        LichSuThanhToan thanhToan = new LichSuThanhToan();
        thanhToan.setHoaDon(hoaDon);
        thanhToan.setSoTien(soTien);
        thanhToan.setHinhThuc(hinhThuc);
        thanhToan.setNguoiNop(nguoiNop);
        thanhToan.setGhiChu(ghiChu);
        
        thanhToan = thanhToanRepo.save(thanhToan);

        // Cập nhật số tiền đã đóng
        BigDecimal tongDaDong = thanhToanRepo.sumSoTienByHoaDonId(idHoaDon);
        if (tongDaDong == null) tongDaDong = BigDecimal.ZERO;
        
        hoaDon.setSoTienDaDong(tongDaDong);
        
        // Cập nhật trạng thái
        if (tongDaDong.compareTo(hoaDon.getTongTienPhaiThu()) >= 0) {
            hoaDon.setTrangThai("Đã đóng");
        } else if (tongDaDong.compareTo(BigDecimal.ZERO) > 0) {
            hoaDon.setTrangThai("Đang nợ");
        } else {
            hoaDon.setTrangThai("Chưa đóng");
        }
        
        hoaDonRepo.save(hoaDon);
        return thanhToan;
    }

    public HoaDon getById(@NonNull Integer id) {
        return hoaDonRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + id));
    }

    public Page<HoaDon> findAll(@NonNull Pageable pageable) {
        return hoaDonRepo.findAll(pageable);
    }

    public Page<HoaDon> findByHoGiaDinh(Integer idHoGiaDinh, @NonNull Pageable pageable) {
        return hoaDonRepo.findByHoGiaDinhIdWithDetails(idHoGiaDinh, pageable);
    }

    public Page<HoaDon> search(Integer idHoGiaDinh, Integer idDotThu, String trangThai, @NonNull Pageable pageable) {
        return hoaDonRepo.search(idHoGiaDinh, idDotThu, trangThai, pageable);
    }

    public List<LichSuThanhToan> getLichSuThanhToan(Integer idHoaDon) {
        return thanhToanRepo.findByHoaDonId(idHoaDon);
    }

    /**
     * Tìm hóa đơn của một hộ trong một đợt thu cụ thể.
     * Sử dụng JOIN FETCH để load đầy đủ data, tránh LazyInitializationException.
     */
    public java.util.Optional<HoaDon> findByHoGiaDinhAndDotThu(Integer idHoGiaDinh, Integer idDotThu) {
        return hoaDonRepo.findByHoGiaDinhIdAndDotThuIdWithDetails(idHoGiaDinh, idDotThu);
    }
}

