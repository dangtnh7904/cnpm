package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.DinhMucThu;
import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.entity.LoaiPhi;
import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.DinhMucThuRepository;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import com.nhom33.quanlychungcu.repository.LoaiPhiRepository;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service: Quản lý Định Mức Thu.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Mỗi hộ gia đình có định mức cho từng loại phí (số lượng).
 * - Tính tiền = SoLuong * DonGiaApDung.
 * - DonGiaApDung lấy từ BangGiaService với logic ưu tiên:
 *   1. BangGiaDichVu (giá riêng theo tòa nhà).
 *   2. LoaiPhi.DonGia (giá mặc định).
 */
@Service
public class DinhMucThuService {

    private final DinhMucThuRepository repo;
    private final HoGiaDinhRepository hoGiaDinhRepo;
    private final LoaiPhiRepository loaiPhiRepo;
    private final ToaNhaRepository toaNhaRepo;
    private final BangGiaService bangGiaService;

    public DinhMucThuService(
            DinhMucThuRepository repo,
            HoGiaDinhRepository hoGiaDinhRepo,
            LoaiPhiRepository loaiPhiRepo,
            ToaNhaRepository toaNhaRepo,
            BangGiaService bangGiaService) {
        this.repo = repo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
        this.loaiPhiRepo = loaiPhiRepo;
        this.toaNhaRepo = toaNhaRepo;
        this.bangGiaService = bangGiaService;
    }

    // ===== CREATE =====

    @Transactional
    public DinhMucThu create(DinhMucThu dinhMuc) {
        // Validate hộ gia đình
        HoGiaDinh hoGiaDinh = hoGiaDinhRepo.findById(dinhMuc.getHoGiaDinh().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình"));
        
        // Validate loại phí
        LoaiPhi loaiPhi = loaiPhiRepo.findById(dinhMuc.getLoaiPhi().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí"));
        
        // Kiểm tra đã có định mức chưa
        Optional<DinhMucThu> existing = repo.findByHoGiaDinhIdAndLoaiPhiId(
            hoGiaDinh.getId(), loaiPhi.getId());
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Hộ gia đình đã có định mức cho loại phí này");
        }
        
        dinhMuc.setHoGiaDinh(hoGiaDinh);
        dinhMuc.setLoaiPhi(loaiPhi);
        
        return repo.save(dinhMuc);
    }

    // ===== UPDATE =====

    @Transactional
    public DinhMucThu update(@NonNull Integer id, DinhMucThu updated) {
        DinhMucThu exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy định mức thu với ID: " + id));
        
        exist.setSoLuong(updated.getSoLuong());
        exist.setGhiChu(updated.getGhiChu());
        
        return repo.save(exist);
    }

    /**
     * Cập nhật số lượng cho định mức.
     */
    @Transactional
    public DinhMucThu updateSoLuong(@NonNull Integer id, Double soLuong) {
        DinhMucThu exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy định mức thu với ID: " + id));
        
        exist.setSoLuong(soLuong);
        return repo.save(exist);
    }

    // ===== DELETE =====

    @Transactional
    public void delete(@NonNull Integer id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy định mức thu với ID: " + id);
        }
        repo.deleteById(id);
    }

    // ===== READ =====

    public DinhMucThu getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy định mức thu với ID: " + id));
    }

    public List<DinhMucThu> findByHoGiaDinh(@NonNull Integer idHoGiaDinh) {
        return repo.findByHoGiaDinhId(idHoGiaDinh);
    }

    public Page<DinhMucThu> findByHoGiaDinh(@NonNull Integer idHoGiaDinh, @NonNull Pageable pageable) {
        return repo.findByHoGiaDinhId(idHoGiaDinh, pageable);
    }

    public Optional<DinhMucThu> findByHoGiaDinhAndLoaiPhi(Integer hoGiaDinhId, Integer loaiPhiId) {
        return repo.findByHoGiaDinhIdAndLoaiPhiId(hoGiaDinhId, loaiPhiId);
    }

    // ===== BULK ACTIONS =====

    /**
     * Tạo định mức hàng loạt cho tất cả hộ gia đình trong một tòa nhà.
     * 
     * @param toaNhaId   ID tòa nhà
     * @param loaiPhiId  ID loại phí
     * @param soLuong    Số lượng mặc định
     * @return Số lượng định mức được tạo
     */
    @Transactional
    public int createBulkByToaNha(@NonNull Integer toaNhaId, @NonNull Integer loaiPhiId, Double soLuong) {
        // Validate tòa nhà
        ToaNha toaNha = toaNhaRepo.findById(toaNhaId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + toaNhaId));
        
        // Validate loại phí
        LoaiPhi loaiPhi = loaiPhiRepo.findById(loaiPhiId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + loaiPhiId));
        
        // Lấy tất cả hộ gia đình trong tòa nhà
        List<HoGiaDinh> households = hoGiaDinhRepo.findByToaNhaId(toaNhaId);
        
        int count = 0;
        for (HoGiaDinh household : households) {
            // Kiểm tra đã có định mức chưa
            if (repo.findByHoGiaDinhIdAndLoaiPhiId(household.getId(), loaiPhiId).isEmpty()) {
                DinhMucThu dinhMuc = new DinhMucThu();
                dinhMuc.setHoGiaDinh(household);
                dinhMuc.setLoaiPhi(loaiPhi);
                dinhMuc.setSoLuong(soLuong != null ? soLuong : 1.0);
                repo.save(dinhMuc);
                count++;
            }
        }
        
        return count;
    }

    /**
     * Cập nhật số lượng hàng loạt cho tất cả hộ gia đình trong tòa nhà.
     */
    @Transactional
    public int updateBulkByToaNha(@NonNull Integer toaNhaId, @NonNull Integer loaiPhiId, Double soLuong) {
        // Lấy tất cả hộ gia đình trong tòa nhà
        List<HoGiaDinh> households = hoGiaDinhRepo.findByToaNhaId(toaNhaId);
        
        int count = 0;
        for (HoGiaDinh household : households) {
            Optional<DinhMucThu> existing = repo.findByHoGiaDinhIdAndLoaiPhiId(household.getId(), loaiPhiId);
            if (existing.isPresent()) {
                DinhMucThu dinhMuc = existing.get();
                dinhMuc.setSoLuong(soLuong);
                repo.save(dinhMuc);
                count++;
            }
        }
        
        return count;
    }

    // ===== PRICE CALCULATION =====

    /**
     * Tính thành tiền cho một định mức.
     * 
     * LOGIC:
     * - ThanhTien = SoLuong * DonGiaApDung
     * - DonGiaApDung lấy từ BangGiaService (logic ưu tiên).
     */
    public BigDecimal tinhThanhTien(@NonNull DinhMucThu dinhMuc) {
        Integer loaiPhiId = dinhMuc.getLoaiPhi().getId();
        Integer toaNhaId = dinhMuc.getHoGiaDinh().getToaNha().getId();
        Double soLuong = dinhMuc.getSoLuong() != null ? dinhMuc.getSoLuong() : 0.0;
        
        // Lấy đơn giá áp dụng (với logic ưu tiên)
        BigDecimal donGia = bangGiaService.getDonGiaApDung(loaiPhiId, toaNhaId);
        
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    /**
     * Tính tổng tiền của một hộ gia đình cho tất cả định mức.
     */
    public BigDecimal tinhTongTien(@NonNull Integer hoGiaDinhId) {
        List<DinhMucThu> dinhMucs = findByHoGiaDinh(hoGiaDinhId);
        
        return dinhMucs.stream()
            .map(this::tinhThanhTien)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * DTO chứa thông tin định mức với giá đã tính.
     */
    public static class DinhMucWithPrice {
        public DinhMucThu dinhMuc;
        public BigDecimal donGiaApDung;
        public BigDecimal thanhTien;
        public boolean isCustomPrice;
        
        public DinhMucWithPrice(DinhMucThu dinhMuc, BigDecimal donGiaApDung, 
                               BigDecimal thanhTien, boolean isCustomPrice) {
            this.dinhMuc = dinhMuc;
            this.donGiaApDung = donGiaApDung;
            this.thanhTien = thanhTien;
            this.isCustomPrice = isCustomPrice;
        }
    }

    /**
     * Lấy danh sách định mức của hộ gia đình kèm giá đã tính.
     */
    public List<DinhMucWithPrice> findByHoGiaDinhWithPrice(@NonNull Integer hoGiaDinhId) {
        List<DinhMucThu> dinhMucs = findByHoGiaDinh(hoGiaDinhId);
        List<DinhMucWithPrice> result = new ArrayList<>();
        
        for (DinhMucThu dinhMuc : dinhMucs) {
            Integer loaiPhiId = dinhMuc.getLoaiPhi().getId();
            Integer toaNhaId = dinhMuc.getHoGiaDinh().getToaNha().getId();
            
            BigDecimal donGia = bangGiaService.getDonGiaApDung(loaiPhiId, toaNhaId);
            boolean isCustomPrice = bangGiaService.findByLoaiPhiAndToaNha(loaiPhiId, toaNhaId).isPresent();
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(dinhMuc.getSoLuong() != null ? dinhMuc.getSoLuong() : 0.0));
            
            result.add(new DinhMucWithPrice(dinhMuc, donGia, thanhTien, isCustomPrice));
        }
        
        return result;
    }
}

