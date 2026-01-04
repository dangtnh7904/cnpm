package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.BangGiaResponseDTO;
import com.nhom33.quanlychungcu.dto.CauHinhGiaDTO;
import com.nhom33.quanlychungcu.dto.ChiTietGiaDTO;
import com.nhom33.quanlychungcu.entity.BangGiaDichVu;
import com.nhom33.quanlychungcu.entity.LoaiPhi;
import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.repository.BangGiaDichVuRepository;
import com.nhom33.quanlychungcu.repository.LoaiPhiRepository;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service: Quản lý Bảng giá dịch vụ theo tòa nhà.
 * 
 * LOGIC NGHIỆP VỤ v4.1:
 * - Loại phí thuộc về Manager (nguoiQuanLy), có thể là:
 *   + Phí CHUNG: toaNha = NULL, áp dụng cho tất cả tòa của Manager
 *   + Phí RIÊNG: toaNha != NULL, chỉ áp dụng cho tòa đó
 * - BangGiaDichVu cho phép cấu hình giá riêng cho từng tòa nhà
 * - Ưu tiên giá: BangGiaDichVu > LoaiPhi.DonGia (giá mặc định)
 */
@Service
public class BangGiaService {

    private final BangGiaDichVuRepository bangGiaRepository;
    private final LoaiPhiRepository loaiPhiRepository;
    private final ToaNhaRepository toaNhaRepository;
    private final SecurityHelper securityHelper;

    public BangGiaService(
            BangGiaDichVuRepository bangGiaRepository,
            LoaiPhiRepository loaiPhiRepository,
            ToaNhaRepository toaNhaRepository,
            SecurityHelper securityHelper) {
        this.bangGiaRepository = bangGiaRepository;
        this.loaiPhiRepository = loaiPhiRepository;
        this.toaNhaRepository = toaNhaRepository;
        this.securityHelper = securityHelper;
    }

    // ===== CORE: Lấy giá với logic ưu tiên =====

    /**
     * Lấy đơn giá áp dụng cho một loại phí tại một tòa nhà.
     * 
     * LOGIC ƯU TIÊN:
     * 1. Nếu có giá riêng trong BangGiaDichVu -> trả về giá riêng.
     * 2. Nếu không có -> trả về giá mặc định từ LoaiPhi.donGia.
     * 3. Nếu không có cả hai -> trả về 0 (để tránh NPE).
     * 
     * @param loaiPhiId ID loại phí
     * @param toaNhaId  ID tòa nhà (có thể null)
     * @return Đơn giá áp dụng, hoặc BigDecimal.ZERO nếu không tìm thấy
     */
    public BigDecimal getDonGiaApDung(Integer loaiPhiId, Integer toaNhaId) {
        // Validate input
        if (loaiPhiId == null) {
            return BigDecimal.ZERO;
        }
        
        // Ưu tiên 1: Tìm giá riêng trong BangGiaDichVu (nếu có toaNhaId)
        if (toaNhaId != null) {
            Optional<BigDecimal> giaRieng = bangGiaRepository.findDonGiaByLoaiPhiAndToaNha(loaiPhiId, toaNhaId);
            if (giaRieng.isPresent() && giaRieng.get() != null) {
                return giaRieng.get();
            }
        }

        // Ưu tiên 2: Lấy giá mặc định từ LoaiPhi
        Optional<LoaiPhi> loaiPhiOpt = loaiPhiRepository.findById(loaiPhiId);
        if (loaiPhiOpt.isPresent() && loaiPhiOpt.get().getDonGia() != null) {
            return loaiPhiOpt.get().getDonGia();
        }
        
        // Fallback: Trả về 0 để tránh NullPointerException
        return BigDecimal.ZERO;
    }

    /**
     * Lấy đơn giá áp dụng, trả về Optional để caller xử lý.
     */
    public Optional<BigDecimal> findDonGiaApDung(Integer loaiPhiId, Integer toaNhaId) {
        BigDecimal donGia = getDonGiaApDung(loaiPhiId, toaNhaId);
        return donGia.compareTo(BigDecimal.ZERO) > 0 ? Optional.of(donGia) : Optional.empty();
    }

    // ===== CRUD Operations =====

    /**
     * Lấy tất cả bảng giá.
     */
    public List<BangGiaDichVu> findAll() {
        return bangGiaRepository.findAllWithDetails();
    }

    /**
     * Lấy bảng giá theo ID.
     */
    public Optional<BangGiaDichVu> findById(Integer id) {
        return bangGiaRepository.findById(id);
    }

    /**
     * Lấy bảng giá theo loại phí và tòa nhà.
     */
    public Optional<BangGiaDichVu> findByLoaiPhiAndToaNha(Integer loaiPhiId, Integer toaNhaId) {
        return bangGiaRepository.findByLoaiPhiIdAndToaNhaId(loaiPhiId, toaNhaId);
    }

    /**
     * Lấy tất cả bảng giá của một tòa nhà.
     */
    public List<BangGiaDichVu> findByToaNha(Integer toaNhaId) {
        return bangGiaRepository.findByToaNhaIdWithDetails(toaNhaId);
    }

    /**
     * Lấy tất cả bảng giá của một loại phí.
     */
    public List<BangGiaDichVu> findByLoaiPhi(Integer loaiPhiId) {
        return bangGiaRepository.findByLoaiPhiId(loaiPhiId);
    }

    // ===== BULK UPSERT =====

    /**
     * Cấu hình giá hàng loạt cho một tòa nhà.
     * 
     * LOGIC:
     * - Với mỗi (loaiPhiId, donGiaRieng) trong danh sách:
     *   + Nếu đã có bảng giá -> Update donGia.
     *   + Nếu chưa có -> Insert mới.
     * 
     * @param cauHinhGiaDTO DTO chứa toaNhaId và danh sách giá
     * @return Số lượng bản ghi được upsert
     */
    @Transactional
    public int cauHinhGiaTheoToaNha(CauHinhGiaDTO cauHinhGiaDTO) {
        Integer toaNhaId = cauHinhGiaDTO.getToaNhaId();
        
        // Validate tòa nhà
        ToaNha toaNha = toaNhaRepository.findById(toaNhaId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà với ID: " + toaNhaId));

        int count = 0;
        for (ChiTietGiaDTO chiTiet : cauHinhGiaDTO.getDanhSachGia()) {
            upsertBangGia(chiTiet.getLoaiPhiId(), toaNha, chiTiet.getDonGiaRieng(), chiTiet.getGhiChu());
            count++;
        }

        return count;
    }

    /**
     * Upsert một bảng giá.
     */
    @Transactional
    public BangGiaDichVu upsertBangGia(Integer loaiPhiId, Integer toaNhaId, BigDecimal donGia, String ghiChu) {
        ToaNha toaNha = toaNhaRepository.findById(toaNhaId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà với ID: " + toaNhaId));
        
        return upsertBangGia(loaiPhiId, toaNha, donGia, ghiChu);
    }

    /**
     * Upsert một bảng giá với ToaNha entity.
     */
    @Transactional
    public BangGiaDichVu upsertBangGia(Integer loaiPhiId, ToaNha toaNha, BigDecimal donGia, String ghiChu) {
        // Validate loại phí
        LoaiPhi loaiPhi = loaiPhiRepository.findById(loaiPhiId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phí với ID: " + loaiPhiId));

        // Tìm bảng giá hiện có
        Optional<BangGiaDichVu> existingOpt = bangGiaRepository
                .findByLoaiPhiIdAndToaNhaId(loaiPhiId, toaNha.getId());

        BangGiaDichVu bangGia;
        if (existingOpt.isPresent()) {
            // Update
            bangGia = existingOpt.get();
            bangGia.setDonGia(donGia);
            bangGia.setNgayApDung(LocalDateTime.now());
            if (ghiChu != null) {
                bangGia.setGhiChu(ghiChu);
            }
        } else {
            // Insert
            bangGia = new BangGiaDichVu();
            bangGia.setLoaiPhi(loaiPhi);
            bangGia.setToaNha(toaNha);
            bangGia.setDonGia(donGia);
            bangGia.setNgayApDung(LocalDateTime.now());
            bangGia.setGhiChu(ghiChu);
        }

        return bangGiaRepository.save(bangGia);
    }

    // ===== DELETE =====

    /**
     * Xóa bảng giá theo ID.
     */
    @Transactional
    public void deleteById(Integer id) {
        bangGiaRepository.deleteById(id);
    }

    /**
     * Xóa bảng giá theo loại phí và tòa nhà.
     */
    @Transactional
    public void deleteByLoaiPhiAndToaNha(Integer loaiPhiId, Integer toaNhaId) {
        bangGiaRepository.deleteByLoaiPhiIdAndToaNhaId(loaiPhiId, toaNhaId);
    }

    /**
     * Xóa tất cả bảng giá của một loại phí.
     */
    @Transactional
    public void deleteByLoaiPhi(Integer loaiPhiId) {
        bangGiaRepository.deleteByLoaiPhiId(loaiPhiId);
    }

    /**
     * Xóa tất cả bảng giá của một tòa nhà.
     */
    @Transactional
    public void deleteByToaNha(Integer toaNhaId) {
        bangGiaRepository.deleteByToaNhaId(toaNhaId);
    }

    // ===== RESPONSE BUILDERS =====

    /**
     * Lấy tất cả bảng giá của một tòa nhà dưới dạng DTO.
     * Multi-tenancy v4.1: Lấy phí CHUNG của Manager + phí RIÊNG của tòa.
     * Bao gồm cả loại phí chưa có giá riêng (dùng giá mặc định).
     */
    public List<BangGiaResponseDTO> getBangGiaFullByToaNha(Integer toaNhaId) {
        // Validate tòa nhà
        ToaNha toaNha = toaNhaRepository.findById(toaNhaId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà với ID: " + toaNhaId));

        // Multi-tenancy v4.1: Lấy Manager từ tòa nhà (không phải từ user đăng nhập)
        // Vì Accountant/Admin cũng có thể xem bảng giá của tòa nhà
        Integer managerId = toaNha.getNguoiQuanLy() != null ? toaNha.getNguoiQuanLy().getId() : null;
        
        List<LoaiPhi> allLoaiPhi;
        if (managerId != null) {
            // Lấy phí CHUNG của Manager + phí RIÊNG của tòa
            allLoaiPhi = loaiPhiRepository.findActiveByNguoiQuanLyAndToaNha(managerId, toaNhaId);
        } else {
            // Tòa nhà chưa có Manager -> trả về rỗng
            allLoaiPhi = new ArrayList<>();
        }

        // Lấy tất cả bảng giá của tòa nhà
        Map<Integer, BangGiaDichVu> bangGiaMap = new HashMap<>();
        for (BangGiaDichVu bg : bangGiaRepository.findByToaNhaId(toaNhaId)) {
            bangGiaMap.put(bg.getLoaiPhi().getId(), bg);
        }

        // Build response
        List<BangGiaResponseDTO> result = new ArrayList<>();
        for (LoaiPhi loaiPhi : allLoaiPhi) {
            BangGiaDichVu bangGia = bangGiaMap.get(loaiPhi.getId());
            
            if (bangGia != null) {
                // Có giá riêng
                result.add(BangGiaResponseDTO.withCustomPrice(
                        loaiPhi.getId(), loaiPhi.getTenLoaiPhi(), loaiPhi.getDonViTinh(), loaiPhi.getDonGia(),
                        toaNhaId, toaNha.getTenToaNha(),
                        bangGia.getId(), bangGia.getDonGia(), bangGia.getNgayApDung(), bangGia.getGhiChu()
                ));
            } else {
                // Dùng giá mặc định
                result.add(BangGiaResponseDTO.withDefaultPrice(
                        loaiPhi.getId(), loaiPhi.getTenLoaiPhi(), loaiPhi.getDonViTinh(), loaiPhi.getDonGia(),
                        toaNhaId, toaNha.getTenToaNha()
                ));
            }
        }

        return result;
    }
}
