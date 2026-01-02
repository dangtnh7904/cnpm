package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.ChiSoInputDTO;
import com.nhom33.quanlychungcu.dto.SaveChiSoRequestDTO;
import com.nhom33.quanlychungcu.entity.*;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service: Quản lý chỉ số Điện Nước.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Chuẩn bị danh sách nhập liệu: Lấy các hộ trong tòa nhà, tự động điền chỉ số cũ
 * - Chỉ số cũ = ChiSoMoi của đợt thu trước (hoặc 0 nếu chưa có)
 * - Khi lưu chỉ số: Tính tiêu thụ, thành tiền và cập nhật vào hóa đơn
 * - Đơn giá ưu tiên: BangGiaDichVu (theo tòa nhà) > LoaiPhi.DonGia (mặc định)
 */
@Service
public class ChiSoDienNuocService {

    private final ChiSoDienNuocRepository chiSoRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final DotThuRepository dotThuRepository;
    private final LoaiPhiRepository loaiPhiRepository;
    private final ToaNhaRepository toaNhaRepository;
    private final BangGiaService bangGiaService;
    private final HoaDonRepository hoaDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;

    public ChiSoDienNuocService(
            ChiSoDienNuocRepository chiSoRepository,
            HoGiaDinhRepository hoGiaDinhRepository,
            DotThuRepository dotThuRepository,
            LoaiPhiRepository loaiPhiRepository,
            ToaNhaRepository toaNhaRepository,
            BangGiaService bangGiaService,
            HoaDonRepository hoaDonRepository,
            ChiTietHoaDonRepository chiTietHoaDonRepository) {
        this.chiSoRepository = chiSoRepository;
        this.hoGiaDinhRepository = hoGiaDinhRepository;
        this.dotThuRepository = dotThuRepository;
        this.loaiPhiRepository = loaiPhiRepository;
        this.toaNhaRepository = toaNhaRepository;
        this.bangGiaService = bangGiaService;
        this.hoaDonRepository = hoaDonRepository;
        this.chiTietHoaDonRepository = chiTietHoaDonRepository;
    }

    // ===== Chuẩn bị danh sách nhập liệu =====

    /**
     * Lấy danh sách các hộ gia đình cần nhập chỉ số.
     * Tự động điền chỉ số cũ từ đợt thu trước.
     * Chỉ lấy các hộ thuộc tòa nhà của đợt thu.
     * 
     * @param dotThuId  ID đợt thu
     * @param loaiPhiId ID loại phí (Điện hoặc Nước)
     * @return Danh sách ChiSoInputDTO
     */
    @Transactional(readOnly = true)
    public List<ChiSoInputDTO> prepareInput(Integer dotThuId, Integer loaiPhiId) {
        // Validate entities exist
        DotThu dotThu = dotThuRepository.findById(dotThuId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + dotThuId));
        
        LoaiPhi loaiPhi = loaiPhiRepository.findById(loaiPhiId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + loaiPhiId));

        // Lấy tòa nhà từ đợt thu (QUAN TRỌNG: chỉ lấy hộ của tòa này)
        ToaNha toaNha = dotThu.getToaNha();
        if (toaNha == null) {
            throw new IllegalStateException("Đợt thu chưa được gán tòa nhà");
        }
        Integer toaNhaId = toaNha.getId();

        // Lấy danh sách hộ gia đình thuộc tòa nhà của đợt thu
        List<HoGiaDinh> danhSachHo = hoGiaDinhRepository.findByToaNhaId(toaNhaId);

        // Lấy các chỉ số đã nhập trong đợt thu này
        Map<Integer, ChiSoDienNuoc> chiSoHienTai = new HashMap<>();
        List<ChiSoDienNuoc> existingRecords = chiSoRepository.findByDotThuAndLoaiPhiAndToaNha(dotThuId, loaiPhiId, toaNhaId);
        
        for (ChiSoDienNuoc cs : existingRecords) {
            chiSoHienTai.put(cs.getHoGiaDinh().getId(), cs);
        }

        // Build danh sách kết quả
        List<ChiSoInputDTO> result = new ArrayList<>();
        
        for (HoGiaDinh ho : danhSachHo) {
            Integer hoId = ho.getId();
            
            // Lấy đơn giá áp dụng cho tòa nhà này
            BigDecimal donGia = bangGiaService.getDonGiaApDung(loaiPhiId, toaNhaId);
            
            Integer chiSoCu;
            Integer chiSoMoi = null;
            
            if (chiSoHienTai.containsKey(hoId)) {
                // Đã có bản ghi trong đợt này
                ChiSoDienNuoc cs = chiSoHienTai.get(hoId);
                chiSoCu = cs.getChiSoCu();
                chiSoMoi = cs.getChiSoMoi();
            } else {
                // Chưa có bản ghi -> tìm chỉ số mới nhất từ đợt trước
                chiSoCu = findLatestChiSoMoi(hoId, loaiPhiId, dotThuId);
            }
            
            ChiSoInputDTO dto = new ChiSoInputDTO(
                    hoId,
                    ho.getMaHoGiaDinh(),
                    ho.getTenChuHo(),
                    ho.getSoCanHo(),
                    chiSoCu,
                    chiSoMoi,
                    donGia
            );
            
            result.add(dto);
        }
        
        // Sắp xếp theo mã hộ
        result.sort(Comparator.comparing(ChiSoInputDTO::getMaHoGiaDinh));
        
        return result;
    }

    /**
     * Tìm chỉ số mới nhất của hộ gia đình từ các đợt thu trước.
     * Dùng làm chỉ số cũ cho đợt thu hiện tại.
     */
    private Integer findLatestChiSoMoi(Integer hoGiaDinhId, Integer loaiPhiId, Integer excludeDotThuId) {
        List<ChiSoDienNuoc> latestList = chiSoRepository.findLatestByHoGiaDinhAndLoaiPhi(
                hoGiaDinhId, loaiPhiId, excludeDotThuId);
        
        if (latestList.isEmpty()) {
            return 0; // Chưa có lịch sử -> bắt đầu từ 0
        }
        
        return latestList.get(0).getChiSoMoi();
    }

    // ===== Lưu chỉ số hàng loạt =====

    /**
     * Lưu danh sách chỉ số và cập nhật hóa đơn.
     * 
     * @param request Request chứa đợt thu, loại phí và danh sách chỉ số
     * @return Số bản ghi đã lưu thành công
     */
    @Transactional
    public int saveAll(SaveChiSoRequestDTO request) {
        Integer dotThuId = request.getDotThuId();
        Integer loaiPhiId = request.getLoaiPhiId();
        
        // Validate
        DotThu dotThu = dotThuRepository.findById(dotThuId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + dotThuId));
        
        LoaiPhi loaiPhi = loaiPhiRepository.findById(loaiPhiId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + loaiPhiId));

        if (request.getDanhSachChiSo() == null || request.getDanhSachChiSo().isEmpty()) {
            return 0;
        }

        int savedCount = 0;
        
        for (SaveChiSoRequestDTO.ChiSoItemDTO item : request.getDanhSachChiSo()) {
            Integer hoGiaDinhId = item.getHoGiaDinhId();
            Integer chiSoCu = item.getChiSoCu();
            Integer chiSoMoi = item.getChiSoMoi();
            
            // Validate chỉ số
            if (chiSoMoi == null) {
                continue; // Bỏ qua nếu chưa nhập
            }
            
            if (chiSoMoi < (chiSoCu != null ? chiSoCu : 0)) {
                throw new IllegalArgumentException(
                        "Chỉ số mới phải >= chỉ số cũ cho hộ ID: " + hoGiaDinhId);
            }
            
            HoGiaDinh hoGiaDinh = hoGiaDinhRepository.findById(hoGiaDinhId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + hoGiaDinhId));
            
            // Tìm hoặc tạo bản ghi ChiSoDienNuoc
            ChiSoDienNuoc chiSo = chiSoRepository
                    .findByHoGiaDinhIdAndDotThuIdAndLoaiPhiId(hoGiaDinhId, dotThuId, loaiPhiId)
                    .orElseGet(() -> {
                        ChiSoDienNuoc newRecord = new ChiSoDienNuoc(hoGiaDinh, dotThu, loaiPhi);
                        return newRecord;
                    });
            
            chiSo.setChiSoCu(chiSoCu != null ? chiSoCu : 0);
            chiSo.setChiSoMoi(chiSoMoi);
            
            chiSoRepository.save(chiSo);
            
            // Tính tiêu thụ và thành tiền
            int tieuThu = chiSoMoi - (chiSoCu != null ? chiSoCu : 0);
            BigDecimal donGia = bangGiaService.getDonGiaApDung(loaiPhiId, hoGiaDinh.getToaNha().getId());
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(tieuThu));
            
            // Cập nhật hóa đơn
            updateHoaDon(hoGiaDinh, dotThu, loaiPhi, tieuThu, donGia, thanhTien);
            
            savedCount++;
        }
        
        return savedCount;
    }

    /**
     * Cập nhật chi tiết hóa đơn sau khi lưu chỉ số.
     */
    private void updateHoaDon(HoGiaDinh hoGiaDinh, DotThu dotThu, LoaiPhi loaiPhi, 
                               int soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        // Tìm hoặc tạo hóa đơn cho hộ trong đợt thu này
        HoaDon hoaDon = hoaDonRepository.findByHoGiaDinhIdAndDotThuId(hoGiaDinh.getId(), dotThu.getId())
                .orElseGet(() -> {
                    HoaDon newHoaDon = new HoaDon();
                    newHoaDon.setHoGiaDinh(hoGiaDinh);
                    newHoaDon.setDotThu(dotThu);
                    newHoaDon.setTrangThai("ChuaThanhToan");
                    newHoaDon.setTongTienPhaiThu(BigDecimal.ZERO);
                    newHoaDon.setSoTienDaDong(BigDecimal.ZERO);
                    return hoaDonRepository.save(newHoaDon);
                });
        
        // Tìm hoặc tạo chi tiết hóa đơn cho loại phí này
        ChiTietHoaDon chiTiet = chiTietHoaDonRepository
                .findByHoaDonIdAndLoaiPhiId(hoaDon.getId(), loaiPhi.getId())
                .orElseGet(() -> {
                    ChiTietHoaDon newChiTiet = new ChiTietHoaDon();
                    newChiTiet.setHoaDon(hoaDon);
                    newChiTiet.setLoaiPhi(loaiPhi);
                    return newChiTiet;
                });
        
        // Cập nhật chi tiết
        BigDecimal oldThanhTien = chiTiet.getThanhTien() != null ? chiTiet.getThanhTien() : BigDecimal.ZERO;
        
        chiTiet.setSoLuong(Double.valueOf(soLuong));
        chiTiet.setDonGia(donGia);
        chiTiet.setThanhTien(thanhTien);
        chiTietHoaDonRepository.save(chiTiet);
        
        // Cập nhật tổng tiền hóa đơn
        BigDecimal tongTienMoi = hoaDon.getTongTienPhaiThu()
                .subtract(oldThanhTien)
                .add(thanhTien);
        hoaDon.setTongTienPhaiThu(tongTienMoi);
        
        // Cập nhật trạng thái
        if (tongTienMoi.compareTo(hoaDon.getSoTienDaDong()) <= 0) {
            hoaDon.setTrangThai("DaThanhToan");
        } else if (hoaDon.getSoTienDaDong().compareTo(BigDecimal.ZERO) > 0) {
            hoaDon.setTrangThai("ThanhToanMotPhan");
        } else {
            hoaDon.setTrangThai("ChuaThanhToan");
        }
        
        hoaDonRepository.save(hoaDon);
    }

    // ===== Thống kê =====

    /**
     * Đếm số hộ chưa nhập chỉ số trong đợt thu.
     */
    public long countChuaNhap(Integer dotThuId, Integer loaiPhiId) {
        return chiSoRepository.countChuaNhap(dotThuId, loaiPhiId);
    }

    /**
     * Đếm số hộ đã nhập chỉ số trong đợt thu.
     */
    public long countDaNhap(Integer dotThuId, Integer loaiPhiId) {
        return chiSoRepository.countDaNhap(dotThuId, loaiPhiId);
    }

    /**
     * Lấy thống kê nhập chỉ số trong một đợt thu.
     */
    public Map<String, Object> getStatistics(Integer dotThuId, Integer loaiPhiId) {
        long chuaNhap = countChuaNhap(dotThuId, loaiPhiId);
        long daNhap = countDaNhap(dotThuId, loaiPhiId);
        long tongSo = chuaNhap + daNhap;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("tongSo", tongSo);
        stats.put("daNhap", daNhap);
        stats.put("chuaNhap", chuaNhap);
        stats.put("phanTramHoanThanh", tongSo > 0 ? (daNhap * 100 / tongSo) : 0);
        
        return stats;
    }
}
