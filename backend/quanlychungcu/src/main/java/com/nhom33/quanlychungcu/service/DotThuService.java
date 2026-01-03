package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.DotThuLoaiPhiDTO;
import com.nhom33.quanlychungcu.entity.*;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service: Quản lý Đợt Thu.
 * 
 * LOGIC NGHIỆP VỤ MỚI (Tách rời ghi số và thu tiền):
 * - Mỗi đợt thu thuộc về một tòa nhà cụ thể
 * - Cho phép nhiều tòa nhà có cùng tên đợt thu
 * - Khi "Chốt sổ/Tính tiền", hệ thống query ChiSoDienNuoc theo Tháng/Năm để tính tiền
 * - Nếu chưa có chỉ số của tháng đó -> Báo lỗi hoặc bỏ qua hộ đó
 */
@Service
public class DotThuService {

    private final DotThuRepository repo;
    private final DotThuLoaiPhiRepository dotThuLoaiPhiRepo;
    private final LoaiPhiRepository loaiPhiRepo;
    private final ToaNhaRepository toaNhaRepo;
    private final HoGiaDinhRepository hoGiaDinhRepo;
    private final HoaDonRepository hoaDonRepo;
    private final ChiTietHoaDonRepository chiTietHoaDonRepo;
    private final DinhMucThuRepository dinhMucThuRepo;
    private final ChiSoDienNuocService chiSoService;
    private final BangGiaService bangGiaService;
    
    // Danh sách tên loại phí biến đổi (cần ghi chỉ số theo tháng)
    private static final List<String> UTILITY_FEES = Arrays.asList("Điện", "Nước");

    public DotThuService(DotThuRepository repo, 
                         DotThuLoaiPhiRepository dotThuLoaiPhiRepo,
                         LoaiPhiRepository loaiPhiRepo,
                         ToaNhaRepository toaNhaRepo,
                         HoGiaDinhRepository hoGiaDinhRepo,
                         HoaDonRepository hoaDonRepo,
                         ChiTietHoaDonRepository chiTietHoaDonRepo,
                         DinhMucThuRepository dinhMucThuRepo,
                         ChiSoDienNuocService chiSoService,
                         BangGiaService bangGiaService) {
        this.repo = repo;
        this.dotThuLoaiPhiRepo = dotThuLoaiPhiRepo;
        this.loaiPhiRepo = loaiPhiRepo;
        this.toaNhaRepo = toaNhaRepo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.chiTietHoaDonRepo = chiTietHoaDonRepo;
        this.dinhMucThuRepo = dinhMucThuRepo;
        this.chiSoService = chiSoService;
        this.bangGiaService = bangGiaService;
    }

    @Transactional
    public DotThu create(DotThu dotThu) {
        // Validate tòa nhà bắt buộc
        if (dotThu.getToaNha() == null || dotThu.getToaNha().getId() == null) {
            throw new IllegalArgumentException("Phải chọn tòa nhà cho đợt thu");
        }
        
        // Validate tòa nhà tồn tại
        Integer toaNhaId = dotThu.getToaNha().getId();
        ToaNha toaNha = toaNhaRepo.findById(toaNhaId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + toaNhaId));
        dotThu.setToaNha(toaNha);
        
        // Validate ngày
        if (dotThu.getNgayKetThuc().isBefore(dotThu.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        // Validate trùng tên trong cùng tòa nhà
        Optional<DotThu> existing = repo.findByTenDotThuAndToaNhaId(dotThu.getTenDotThu(), toaNhaId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                "Tên đợt thu '" + dotThu.getTenDotThu() + "' đã tồn tại trong tòa " + toaNha.getTenToaNha());
        }
        
        DotThu saved = repo.save(dotThu);
        
        // KHÔNG tự động thêm phí - Admin sẽ tự chọn loại phí cần thu
        // Đợt thu mới tạo sẽ rỗng danh sách phí
        
        return saved;
    }

    @Transactional
    public DotThu update(@NonNull Integer id, DotThu updated) {
        DotThu exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + id));
        
        if (updated.getNgayKetThuc().isBefore(updated.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        // Nếu thay đổi tên, kiểm tra trùng trong cùng tòa nhà
        if (!exist.getTenDotThu().equals(updated.getTenDotThu())) {
            Integer toaNhaId = exist.getToaNha() != null ? exist.getToaNha().getId() : null;
            if (toaNhaId != null) {
                Optional<DotThu> duplicate = repo.findByTenDotThuAndToaNhaId(updated.getTenDotThu(), toaNhaId);
                if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                    throw new IllegalArgumentException(
                        "Tên đợt thu '" + updated.getTenDotThu() + "' đã tồn tại trong tòa nhà này");
                }
            }
        }
        
        exist.setTenDotThu(updated.getTenDotThu());
        exist.setLoaiDotThu(updated.getLoaiDotThu());
        exist.setNgayBatDau(updated.getNgayBatDau());
        exist.setNgayKetThuc(updated.getNgayKetThuc());
        // Không cho thay đổi tòa nhà sau khi tạo
        
        return repo.save(exist);
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + id);
        }
        repo.deleteById(id);
    }

    public DotThu getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + id));
    }

    public Page<DotThu> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<DotThu> search(String tenDotThu, String loaiDotThu, Integer toaNhaId, LocalDate ngayBatDau, LocalDate ngayKetThuc, @NonNull Pageable pageable) {
        return repo.search(tenDotThu, loaiDotThu, toaNhaId, ngayBatDau, ngayKetThuc, pageable);
    }
    
    // ===== Quản lý loại phí trong đợt thu =====
    
    /**
     * Lấy danh sách loại phí trong đợt thu với giá ưu tiên.
     * 
     * Giá ưu tiên được lấy theo thứ tự:
     * 1. BangGiaDichVu (giá riêng theo tòa nhà) - nếu có
     * 2. LoaiPhi.DonGia (giá mặc định) - nếu không có giá riêng
     */
    public List<DotThuLoaiPhiDTO> getFeesInPeriod(Integer dotThuId) {
        DotThu dotThu = repo.findById(dotThuId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + dotThuId));
        
        Integer toaNhaId = dotThu.getToaNha().getId();
        List<DotThuLoaiPhi> fees = dotThuLoaiPhiRepo.findByDotThuId(dotThuId);
        
        // Convert sang DTO với giá ưu tiên
        List<DotThuLoaiPhiDTO> result = new ArrayList<>();
        for (DotThuLoaiPhi fee : fees) {
            Integer loaiPhiId = fee.getLoaiPhi().getId();
            BigDecimal donGiaMacDinh = fee.getLoaiPhi().getDonGia();
            
            // Lấy giá ưu tiên từ BangGiaService
            BigDecimal donGiaApDung = bangGiaService.getDonGiaApDung(loaiPhiId, toaNhaId);
            
            // Xác định nguồn giá
            String nguonGia = donGiaApDung.compareTo(donGiaMacDinh) != 0 ? "BangGiaDichVu" : "LoaiPhi";
            
            result.add(new DotThuLoaiPhiDTO(fee, donGiaApDung, nguonGia));
        }
        
        return result;
    }
    
    /**
     * Thêm loại phí vào đợt thu.
     * Trả về Map chứa config và flag hasUtilityFee.
     */
    @Transactional
    public Map<String, Object> addFeeToPeriod(Integer dotThuId, Integer loaiPhiId) {
        DotThu dotThu = repo.findById(dotThuId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + dotThuId));
        
        LoaiPhi loaiPhi = loaiPhiRepo.findById(loaiPhiId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + loaiPhiId));
        
        // Kiểm tra đã tồn tại chưa
        if (dotThuLoaiPhiRepo.existsByDotThuIdAndLoaiPhiId(dotThuId, loaiPhiId)) {
            throw new IllegalArgumentException("Loại phí đã tồn tại trong đợt thu này");
        }
        
        DotThuLoaiPhi config = new DotThuLoaiPhi(dotThu, loaiPhi);
        DotThuLoaiPhi saved = dotThuLoaiPhiRepo.save(config);
        
        // Kiểm tra đợt thu có chứa phí biến đổi (Điện/Nước) không
        boolean hasUtilityFee = checkHasUtilityFee(dotThuId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("config", saved);
        result.put("hasUtilityFee", hasUtilityFee);
        result.put("isUtilityFee", UTILITY_FEES.contains(loaiPhi.getTenLoaiPhi()));
        
        return result;
    }
    
    /**
     * Xóa loại phí khỏi đợt thu.
     * Không cho xóa phí bắt buộc (Điện, Nước).
     * Trả về flag hasUtilityFee sau khi xóa.
     */
    @Transactional
    public Map<String, Object> removeFeeFromPeriod(Integer dotThuId, Integer loaiPhiId) {
        if (!repo.existsById(dotThuId)) {
            throw new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + dotThuId);
        }
        
        LoaiPhi loaiPhi = loaiPhiRepo.findById(loaiPhiId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + loaiPhiId));
        
        // Cho phép xóa TẤT CẢ loại phí (bao gồm Điện/Nước)
        // Admin tự quyết định đợt thu nào cần thu phí gì
        
        if (!dotThuLoaiPhiRepo.existsByDotThuIdAndLoaiPhiId(dotThuId, loaiPhiId)) {
            throw new ResourceNotFoundException("Loại phí không tồn tại trong đợt thu này");
        }
        
        dotThuLoaiPhiRepo.deleteByDotThuIdAndLoaiPhiId(dotThuId, loaiPhiId);
        
        // Kiểm tra lại sau khi xóa
        boolean hasUtilityFee = checkHasUtilityFee(dotThuId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Xóa loại phí thành công");
        result.put("hasUtilityFee", hasUtilityFee);
        
        return result;
    }
    
    /**
     * Kiểm tra đợt thu có chứa phí biến đổi (Điện/Nước) không.
     * Dùng để Frontend quyết định hiển thị Tab Ghi Chỉ Số.
     */
    public boolean checkHasUtilityFee(Integer dotThuId) {
        List<DotThuLoaiPhi> fees = dotThuLoaiPhiRepo.findByDotThuId(dotThuId);
        return fees.stream()
            .anyMatch(f -> UTILITY_FEES.contains(f.getLoaiPhi().getTenLoaiPhi()));
    }
    
    /**
     * Lấy danh sách loại phí biến đổi (Điện/Nước) trong đợt thu.
     */
    public List<DotThuLoaiPhi> getUtilityFeesInPeriod(Integer dotThuId) {
        List<DotThuLoaiPhi> fees = dotThuLoaiPhiRepo.findByDotThuId(dotThuId);
        return fees.stream()
            .filter(f -> UTILITY_FEES.contains(f.getLoaiPhi().getTenLoaiPhi()))
            .toList();
    }
    
    /**
     * Kiểm tra loại phí có phải phí biến đổi (cần ghi chỉ số) không.
     * Không còn phí "bắt buộc" - Admin tự quyết định.
     */
    public boolean isUtilityFee(Integer loaiPhiId) {
        return loaiPhiRepo.findById(loaiPhiId)
            .map(lp -> UTILITY_FEES.contains(lp.getTenLoaiPhi()))
            .orElse(false);
    }

    // ===== Tính tiền hóa đơn =====
    
    /**
     * Tính tiền và tạo hóa đơn cho tất cả hộ gia đình trong đợt thu.
     * 
     * LOGIC:
     * - Duyệt qua tất cả hộ trong tòa nhà của đợt thu
     * - Với mỗi loại phí trong đợt thu:
     *   + Nếu là phí biến đổi (Điện/Nước): Sử dụng Thang/Nam của đợt thu để lấy chỉ số
     *   + Nếu là phí cố định: Lấy từ DinhMucThu hoặc diện tích căn hộ
     * 
     * @param dotThuId ID đợt thu (đã lưu sẵn thang và nam)
     * @return Thống kê kết quả tính tiền
     */
    @Transactional
    public Map<String, Object> calculateInvoices(Integer dotThuId) {
        DotThu dotThu = repo.findById(dotThuId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + dotThuId));
        
        ToaNha toaNha = dotThu.getToaNha();
        if (toaNha == null) {
            throw new IllegalStateException("Đợt thu chưa được gán tòa nhà");
        }
        
        // Lấy thang và nam từ đợt thu
        Integer thang = dotThu.getThang();
        Integer nam = dotThu.getNam();
        if (thang == null || nam == null) {
            throw new IllegalStateException("Đợt thu chưa được cấu hình Tháng/Năm để tính phí điện nước");
        }
        
        Integer toaNhaId = toaNha.getId();
        
        // Lấy danh sách hộ gia đình
        List<HoGiaDinh> danhSachHo = hoGiaDinhRepo.findByToaNhaId(toaNhaId);
        
        // Lấy danh sách loại phí trong đợt thu
        List<DotThuLoaiPhi> danhSachPhi = dotThuLoaiPhiRepo.findByDotThuId(dotThuId);
        
        int soHoaDonTao = 0;
        int soHoThieuChiSo = 0;
        List<String> danhSachThieuChiSo = new ArrayList<>();
        
        for (HoGiaDinh ho : danhSachHo) {
            // Tìm hoặc tạo hóa đơn
            HoaDon hoaDon = hoaDonRepo.findByHoGiaDinhIdAndDotThuId(ho.getId(), dotThuId)
                .orElseGet(() -> {
                    HoaDon newHoaDon = new HoaDon();
                    newHoaDon.setHoGiaDinh(ho);
                    newHoaDon.setDotThu(dotThu);
                    newHoaDon.setTrangThai("ChuaThanhToan");
                    newHoaDon.setTongTienPhaiThu(BigDecimal.ZERO);
                    newHoaDon.setSoTienDaDong(BigDecimal.ZERO);
                    return hoaDonRepo.save(newHoaDon);
                });
            
            BigDecimal tongTien = BigDecimal.ZERO;
            boolean thieuChiSo = false;
            
            for (DotThuLoaiPhi config : danhSachPhi) {
                LoaiPhi loaiPhi = config.getLoaiPhi();
                BigDecimal donGia = bangGiaService.getDonGiaApDung(loaiPhi.getId(), toaNhaId);
                BigDecimal thanhTien = BigDecimal.ZERO;
                double soLuong = 0;
                
                if (UTILITY_FEES.contains(loaiPhi.getTenLoaiPhi())) {
                    // Phí biến đổi -> Query chỉ số
                    Integer tieuThu = chiSoService.getTieuThu(ho.getId(), loaiPhi.getId(), thang, nam);
                    
                    if (tieuThu == null) {
                        // Chưa có chỉ số
                        thieuChiSo = true;
                        continue;
                    }
                    
                    soLuong = tieuThu;
                    thanhTien = donGia.multiply(BigDecimal.valueOf(tieuThu));
                } else {
                    // Phí cố định -> Lấy từ DinhMucThu của hộ gia đình
                    Optional<DinhMucThu> dinhMucOpt = dinhMucThuRepo.findByHoGiaDinhIdAndLoaiPhiId(ho.getId(), loaiPhi.getId());
                    if (dinhMucOpt.isPresent()) {
                        soLuong = dinhMucOpt.get().getSoLuong();
                    } else {
                        // Mặc định = 1 nếu không có định mức
                        soLuong = 1.0;
                    }
                    thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
                }
                
                // Tạo hoặc cập nhật chi tiết hóa đơn
                ChiTietHoaDon chiTiet = chiTietHoaDonRepo
                    .findByHoaDonIdAndLoaiPhiId(hoaDon.getId(), loaiPhi.getId())
                    .orElseGet(() -> {
                        ChiTietHoaDon newChiTiet = new ChiTietHoaDon();
                        newChiTiet.setHoaDon(hoaDon);
                        newChiTiet.setLoaiPhi(loaiPhi);
                        return newChiTiet;
                    });
                
                chiTiet.setSoLuong(soLuong);
                chiTiet.setDonGia(donGia);
                chiTiet.setThanhTien(thanhTien);
                chiTietHoaDonRepo.save(chiTiet);
                
                tongTien = tongTien.add(thanhTien);
            }
            
            // Cập nhật tổng tiền hóa đơn
            hoaDon.setTongTienPhaiThu(tongTien);
            if (tongTien.compareTo(hoaDon.getSoTienDaDong()) <= 0 && tongTien.compareTo(BigDecimal.ZERO) > 0) {
                hoaDon.setTrangThai("DaThanhToan");
            } else if (hoaDon.getSoTienDaDong().compareTo(BigDecimal.ZERO) > 0) {
                hoaDon.setTrangThai("ThanhToanMotPhan");
            } else {
                hoaDon.setTrangThai("ChuaThanhToan");
            }
            hoaDonRepo.save(hoaDon);
            
            soHoaDonTao++;
            
            if (thieuChiSo) {
                soHoThieuChiSo++;
                danhSachThieuChiSo.add(ho.getMaHoGiaDinh());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Đã tính tiền cho " + soHoaDonTao + " hộ gia đình");
        result.put("soHoaDonTao", soHoaDonTao);
        result.put("soHoThieuChiSo", soHoThieuChiSo);
        result.put("danhSachThieuChiSo", danhSachThieuChiSo);
        result.put("thang", thang);
        result.put("nam", nam);
        
        return result;
    }
    
    /**
     * Lấy bảng kê chi tiết các khoản phí cho tất cả hộ gia đình trong đợt thu.
     * 
     * @param dotThuId ID đợt thu
     * @return Bảng kê chi tiết { dotThuId, tenDotThu, toaNha, danhSach[], tongCong }
     */
    public Map<String, Object> getBangKe(Integer dotThuId) {
        DotThu dotThu = repo.findById(dotThuId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + dotThuId));
        
        ToaNha toaNha = dotThu.getToaNha();
        if (toaNha == null) {
            throw new IllegalStateException("Đợt thu chưa được gán tòa nhà");
        }
        
        // Lấy tất cả hóa đơn trong đợt thu (với eager fetch HoGiaDinh)
        List<HoaDon> danhSachHoaDon = hoaDonRepo.findByDotThuIdWithHoGiaDinh(dotThuId);
        
        // Lấy danh sách loại phí trong đợt thu để có thứ tự
        List<DotThuLoaiPhi> danhSachPhi = dotThuLoaiPhiRepo.findByDotThuId(dotThuId);
        List<String> loaiPhiOrder = danhSachPhi.stream()
            .map(dlp -> dlp.getLoaiPhi().getTenLoaiPhi())
            .toList();
        
        BigDecimal tongCong = BigDecimal.ZERO;
        List<Map<String, Object>> danhSachHo = new ArrayList<>();
        
        for (HoaDon hoaDon : danhSachHoaDon) {
            HoGiaDinh ho = hoaDon.getHoGiaDinh();
            
            Map<String, Object> hoInfo = new HashMap<>();
            hoInfo.put("hoaDonId", hoaDon.getId());
            hoInfo.put("maHoGiaDinh", ho.getMaHoGiaDinh());
            hoInfo.put("soCanHo", ho.getSoCanHo());
            hoInfo.put("chuHo", ho.getTenChuHo());
            
            // Lấy chi tiết hóa đơn (với eager fetch LoaiPhi)
            List<ChiTietHoaDon> chiTietList = chiTietHoaDonRepo.findByHoaDonIdWithLoaiPhi(hoaDon.getId());
            List<Map<String, Object>> chiTietInfo = new ArrayList<>();
            
            for (ChiTietHoaDon ct : chiTietList) {
                Map<String, Object> ctMap = new HashMap<>();
                ctMap.put("tenLoaiPhi", ct.getLoaiPhi().getTenLoaiPhi());
                ctMap.put("donViTinh", ct.getLoaiPhi().getDonViTinh());
                ctMap.put("soLuong", ct.getSoLuong());
                ctMap.put("donGia", ct.getDonGia());
                ctMap.put("thanhTien", ct.getThanhTien());
                chiTietInfo.add(ctMap);
            }
            
            hoInfo.put("chiTiet", chiTietInfo);
            hoInfo.put("tongTien", hoaDon.getTongTienPhaiThu());
            hoInfo.put("daDong", hoaDon.getSoTienDaDong());
            hoInfo.put("conNo", hoaDon.getTongTienPhaiThu().subtract(hoaDon.getSoTienDaDong()));
            hoInfo.put("trangThai", hoaDon.getTrangThai());
            
            danhSachHo.add(hoInfo);
            tongCong = tongCong.add(hoaDon.getTongTienPhaiThu());
        }
        
        // Sắp xếp theo mã hộ
        danhSachHo.sort((a, b) -> {
            String maA = (String) a.get("maHoGiaDinh");
            String maB = (String) b.get("maHoGiaDinh");
            return maA.compareTo(maB);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("dotThuId", dotThu.getId());
        result.put("tenDotThu", dotThu.getTenDotThu());
        result.put("toaNha", toaNha.getTenToaNha());
        result.put("loaiPhiOrder", loaiPhiOrder);
        result.put("danhSach", danhSachHo);
        result.put("tongCong", tongCong);
        result.put("soHoaDon", danhSachHoaDon.size());
        
        return result;
    }

    /**
     * Export bảng kê ra file CSV (Excel-compatible).
     * 
     * @param dotThuId ID đợt thu
     * @return byte[] nội dung file CSV
     */
    public byte[] exportBangKeToCSV(Integer dotThuId) {
        DotThu dotThu = repo.findById(dotThuId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + dotThuId));
        
        ToaNha toaNha = dotThu.getToaNha();
        if (toaNha == null) {
            throw new IllegalStateException("Đợt thu chưa được gán tòa nhà");
        }

        // Lấy tất cả hóa đơn trong đợt thu
        List<HoaDon> danhSachHoaDon = hoaDonRepo.findByDotThuIdWithHoGiaDinh(dotThuId);
        
        // Lấy danh sách loại phí trong đợt thu
        List<DotThuLoaiPhi> danhSachPhi = dotThuLoaiPhiRepo.findByDotThuId(dotThuId);
        List<String> loaiPhiNames = danhSachPhi.stream()
            .map(dlp -> dlp.getLoaiPhi().getTenLoaiPhi())
            .toList();
        
        // Build CSV content
        StringBuilder csv = new StringBuilder();
        
        // UTF-8 BOM để Excel hiển thị tiếng Việt đúng
        csv.append("\uFEFF");
        
        // Header row
        csv.append("STT,Mã hộ,Căn hộ,Chủ hộ");
        for (String loaiPhi : loaiPhiNames) {
            csv.append(",").append(escapeCSV(loaiPhi));
        }
        csv.append(",Tổng tiền,Đã đóng,Còn nợ,Trạng thái\n");
        
        // Data rows
        int stt = 1;
        BigDecimal tongCongPhaiThu = BigDecimal.ZERO;
        BigDecimal tongCongDaDong = BigDecimal.ZERO;
        
        // Sắp xếp theo mã hộ
        danhSachHoaDon.sort((a, b) -> a.getHoGiaDinh().getMaHoGiaDinh()
            .compareTo(b.getHoGiaDinh().getMaHoGiaDinh()));
        
        for (HoaDon hoaDon : danhSachHoaDon) {
            HoGiaDinh ho = hoaDon.getHoGiaDinh();
            
            csv.append(stt++);
            csv.append(",").append(escapeCSV(ho.getMaHoGiaDinh()));
            csv.append(",").append(escapeCSV(ho.getSoCanHo()));
            csv.append(",").append(escapeCSV(ho.getTenChuHo()));
            
            // Chi tiết từng loại phí
            List<ChiTietHoaDon> chiTietList = chiTietHoaDonRepo.findByHoaDonIdWithLoaiPhi(hoaDon.getId());
            Map<String, BigDecimal> phiMap = new HashMap<>();
            for (ChiTietHoaDon ct : chiTietList) {
                phiMap.put(ct.getLoaiPhi().getTenLoaiPhi(), ct.getThanhTien());
            }
            
            for (String loaiPhi : loaiPhiNames) {
                BigDecimal thanhTien = phiMap.getOrDefault(loaiPhi, BigDecimal.ZERO);
                csv.append(",").append(thanhTien);
            }
            
            csv.append(",").append(hoaDon.getTongTienPhaiThu());
            csv.append(",").append(hoaDon.getSoTienDaDong());
            csv.append(",").append(hoaDon.getTongTienPhaiThu().subtract(hoaDon.getSoTienDaDong()));
            
            String trangThai = switch (hoaDon.getTrangThai()) {
                case "DaThanhToan" -> "Đã thanh toán";
                case "ThanhToanMotPhan" -> "Thanh toán một phần";
                case "ChuaThanhToan" -> "Chưa thanh toán";
                default -> hoaDon.getTrangThai();
            };
            csv.append(",").append(escapeCSV(trangThai));
            csv.append("\n");
            
            tongCongPhaiThu = tongCongPhaiThu.add(hoaDon.getTongTienPhaiThu());
            tongCongDaDong = tongCongDaDong.add(hoaDon.getSoTienDaDong());
        }
        
        // Summary row
        csv.append("\n");
        csv.append(",,,TỔNG CỘNG");
        for (int i = 0; i < loaiPhiNames.size(); i++) {
            csv.append(",");
        }
        csv.append(",").append(tongCongPhaiThu);
        csv.append(",").append(tongCongDaDong);
        csv.append(",").append(tongCongPhaiThu.subtract(tongCongDaDong));
        csv.append(",\n");
        
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    
    /**
     * Escape CSV value (xử lý dấu phẩy và dấu ngoặc kép).
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

