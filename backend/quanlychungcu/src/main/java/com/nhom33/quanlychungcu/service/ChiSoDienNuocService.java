package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.ChiSoInputDTO;
import com.nhom33.quanlychungcu.dto.SaveChiSoRequestDTO;
import com.nhom33.quanlychungcu.entity.*;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service: Quản lý chỉ số Điện Nước.
 * 
 * LOGIC NGHIỆP VỤ MỚI (Tách rời ghi số và thu tiền):
 * - Ghi chỉ số là hoạt động cố định hàng tháng (chốt ngày 24)
 * - Không phụ thuộc vào Đợt thu
 * - Lưu theo Tháng/Năm và Hộ gia đình
 * - Khi tạo Đợt thu có phí Điện/Nước: Query bảng này để tính tiền
 * - Validation: ChiSoMoi >= ChiSoMoi của tháng trước
 */
@Service
public class ChiSoDienNuocService {

    private final ChiSoDienNuocRepository chiSoRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final LoaiPhiRepository loaiPhiRepository;
    private final ToaNhaRepository toaNhaRepository;

    public ChiSoDienNuocService(
            ChiSoDienNuocRepository chiSoRepository,
            HoGiaDinhRepository hoGiaDinhRepository,
            LoaiPhiRepository loaiPhiRepository,
            ToaNhaRepository toaNhaRepository) {
        this.chiSoRepository = chiSoRepository;
        this.hoGiaDinhRepository = hoGiaDinhRepository;
        this.loaiPhiRepository = loaiPhiRepository;
        this.toaNhaRepository = toaNhaRepository;
    }

    // ===== Chuẩn bị danh sách nhập liệu =====

    /**
     * Lấy danh sách các hộ gia đình cần nhập chỉ số cho tháng/năm.
     * Tự động điền chỉ số cũ từ tháng trước.
     * 
     * @param thang     Tháng ghi sổ (1-12)
     * @param nam       Năm ghi sổ
     * @param toaNhaId  ID tòa nhà
     * @param loaiPhiId ID loại phí (Điện hoặc Nước)
     * @return Danh sách ChiSoInputDTO
     */
    @Transactional(readOnly = true)
    public List<ChiSoInputDTO> prepareInput(Integer thang, Integer nam, Integer toaNhaId, Integer loaiPhiId) {
        // Validate
        if (thang < 1 || thang > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1-12");
        }
        
        toaNhaRepository.findById(toaNhaId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + toaNhaId));
        
        loaiPhiRepository.findById(loaiPhiId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + loaiPhiId));

        // Lấy danh sách hộ gia đình thuộc tòa nhà
        List<HoGiaDinh> danhSachHo = hoGiaDinhRepository.findByToaNhaId(toaNhaId);

        // Lấy các chỉ số đã nhập trong tháng này
        Map<Integer, ChiSoDienNuoc> chiSoHienTai = new HashMap<>();
        List<ChiSoDienNuoc> existingRecords = chiSoRepository.findByThangNamAndLoaiPhiAndToaNha(
                thang, nam, loaiPhiId, toaNhaId);
        
        for (ChiSoDienNuoc cs : existingRecords) {
            chiSoHienTai.put(cs.getHoGiaDinh().getId(), cs);
        }

        // Tính tháng trước
        int thangTruoc = thang == 1 ? 12 : thang - 1;
        int namTruoc = thang == 1 ? nam - 1 : nam;

        // Build danh sách kết quả
        List<ChiSoInputDTO> result = new ArrayList<>();
        
        for (HoGiaDinh ho : danhSachHo) {
            Integer hoId = ho.getId();
            
            Integer chiSoCu;
            Integer chiSoMoi = null;
            
            if (chiSoHienTai.containsKey(hoId)) {
                // Đã có bản ghi trong tháng này
                ChiSoDienNuoc cs = chiSoHienTai.get(hoId);
                chiSoMoi = cs.getChiSoMoi();
            }
            
            // Lấy chỉ số cũ từ tháng trước
            chiSoCu = findPreviousMonthChiSo(hoId, loaiPhiId, thangTruoc, namTruoc);
            
            ChiSoInputDTO dto = new ChiSoInputDTO(
                    hoId,
                    ho.getMaHoGiaDinh(),
                    ho.getTenChuHo(),
                    ho.getSoCanHo(),
                    chiSoCu,
                    chiSoMoi
            );
            
            result.add(dto);
        }
        
        // Sắp xếp theo mã hộ
        result.sort(Comparator.comparing(ChiSoInputDTO::getMaHoGiaDinh));
        
        return result;
    }

    /**
     * Tìm chỉ số của tháng trước.
     */
    private Integer findPreviousMonthChiSo(Integer hoGiaDinhId, Integer loaiPhiId, Integer thang, Integer nam) {
        Optional<ChiSoDienNuoc> prev = chiSoRepository.findByHoGiaDinhIdAndLoaiPhiIdAndThangAndNam(
                hoGiaDinhId, loaiPhiId, thang, nam);
        
        if (prev.isPresent()) {
            return prev.get().getChiSoMoi();
        }
        
        // Không có tháng trước -> lấy bản ghi mới nhất
        List<ChiSoDienNuoc> latestList = chiSoRepository.findLatestByHoGiaDinhAndLoaiPhi(hoGiaDinhId, loaiPhiId);
        if (!latestList.isEmpty()) {
            return latestList.get(0).getChiSoMoi();
        }
        
        return 0; // Chưa có lịch sử -> bắt đầu từ 0
    }

    // ===== Lưu chỉ số hàng loạt =====

    /**
     * Lưu danh sách chỉ số cho tháng/năm.
     * CHỈ LƯU CHỈ SỐ - KHÔNG TÍNH TIỀN.
     * 
     * @param request Request chứa tháng, năm, tòa nhà, loại phí và danh sách chỉ số
     * @return Số bản ghi đã lưu thành công
     */
    @Transactional
    public int saveAll(SaveChiSoRequestDTO request) {
        Integer thang = request.getThang();
        Integer nam = request.getNam();
        Integer toaNhaId = request.getToaNhaId();
        Integer loaiPhiId = request.getLoaiPhiId();
        
        // Validate
        if (thang < 1 || thang > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1-12");
        }
        
        toaNhaRepository.findById(toaNhaId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + toaNhaId));
        
        LoaiPhi loaiPhi = loaiPhiRepository.findById(loaiPhiId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + loaiPhiId));

        if (request.getDanhSachChiSo() == null || request.getDanhSachChiSo().isEmpty()) {
            return 0;
        }

        // Tính tháng trước để validate
        int thangTruoc = thang == 1 ? 12 : thang - 1;
        int namTruoc = thang == 1 ? nam - 1 : nam;

        int savedCount = 0;
        
        for (SaveChiSoRequestDTO.ChiSoItemDTO item : request.getDanhSachChiSo()) {
            Integer hoGiaDinhId = item.getHoGiaDinhId();
            Integer chiSoMoi = item.getChiSoMoi();
            
            // Bỏ qua nếu chưa nhập
            if (chiSoMoi == null) {
                continue;
            }
            
            HoGiaDinh hoGiaDinh = hoGiaDinhRepository.findById(hoGiaDinhId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + hoGiaDinhId));
            
            // Validate: ChiSoMoi >= ChiSo tháng trước
            Integer chiSoThangTruoc = findPreviousMonthChiSo(hoGiaDinhId, loaiPhiId, thangTruoc, namTruoc);
            if (chiSoMoi < chiSoThangTruoc) {
                throw new IllegalArgumentException(
                        String.format("Chỉ số mới (%d) phải >= chỉ số tháng trước (%d) cho hộ %s", 
                                chiSoMoi, chiSoThangTruoc, hoGiaDinh.getMaHoGiaDinh()));
            }
            
            // Tìm hoặc tạo bản ghi
            ChiSoDienNuoc chiSo = chiSoRepository
                    .findByHoGiaDinhIdAndLoaiPhiIdAndThangAndNam(hoGiaDinhId, loaiPhiId, thang, nam)
                    .orElseGet(() -> new ChiSoDienNuoc(hoGiaDinh, loaiPhi, thang, nam, chiSoMoi));
            
            chiSo.setChiSoMoi(chiSoMoi);
            chiSoRepository.save(chiSo);
            
            savedCount++;
        }
        
        return savedCount;
    }

    // ===== Thống kê =====

    /**
     * Đếm số hộ đã ghi chỉ số trong tháng/năm theo tòa nhà.
     */
    public long countDaNhap(Integer thang, Integer nam, Integer loaiPhiId, Integer toaNhaId) {
        return chiSoRepository.countByThangNamAndLoaiPhiAndToaNha(thang, nam, loaiPhiId, toaNhaId);
    }

    /**
     * Lấy thống kê nhập chỉ số trong một tháng/năm.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(Integer thang, Integer nam, Integer loaiPhiId, Integer toaNhaId) {
        long daNhap = countDaNhap(thang, nam, loaiPhiId, toaNhaId);
        long tongSoHo = hoGiaDinhRepository.countByToaNhaId(toaNhaId);
        long chuaNhap = tongSoHo - daNhap;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("thang", thang);
        stats.put("nam", nam);
        stats.put("tongSoHo", tongSoHo);
        stats.put("daNhap", daNhap);
        stats.put("chuaNhap", Math.max(0, chuaNhap));
        stats.put("phanTramHoanThanh", tongSoHo > 0 ? (daNhap * 100 / tongSoHo) : 0);
        
        return stats;
    }

    // ===== Methods cho Đợt Thu (DotThuService gọi) =====

    /**
     * Lấy chỉ số của hộ gia đình trong một tháng/năm.
     * Dùng khi tính tiền điện/nước cho Đợt thu.
     * 
     * @param hoGiaDinhId ID hộ gia đình
     * @param loaiPhiId   ID loại phí (Điện/Nước)
     * @param thang       Tháng
     * @param nam         Năm
     * @return ChiSoMoi hoặc null nếu chưa ghi
     */
    public Integer getChiSo(Integer hoGiaDinhId, Integer loaiPhiId, Integer thang, Integer nam) {
        return chiSoRepository.findByHoGiaDinhIdAndLoaiPhiIdAndThangAndNam(hoGiaDinhId, loaiPhiId, thang, nam)
                .map(ChiSoDienNuoc::getChiSoMoi)
                .orElse(null);
    }

    /**
     * Tính lượng tiêu thụ (ChiSo tháng T - ChiSo tháng T-1).
     * 
     * @param hoGiaDinhId ID hộ gia đình
     * @param loaiPhiId   ID loại phí
     * @param thang       Tháng hiện tại
     * @param nam         Năm hiện tại
     * @return Lượng tiêu thụ, hoặc null nếu thiếu dữ liệu
     */
    public Integer getTieuThu(Integer hoGiaDinhId, Integer loaiPhiId, Integer thang, Integer nam) {
        Integer chiSoHienTai = getChiSo(hoGiaDinhId, loaiPhiId, thang, nam);
        if (chiSoHienTai == null) {
            return null; // Chưa ghi chỉ số tháng này
        }
        
        // Tính tháng trước
        int thangTruoc = thang == 1 ? 12 : thang - 1;
        int namTruoc = thang == 1 ? nam - 1 : nam;
        
        Integer chiSoThangTruoc = getChiSo(hoGiaDinhId, loaiPhiId, thangTruoc, namTruoc);
        if (chiSoThangTruoc == null) {
            // Không có chỉ số tháng trước -> tìm bản ghi cũ nhất trước tháng hiện tại
            Optional<ChiSoDienNuoc> prevRecord = chiSoRepository.findPreviousMonth(hoGiaDinhId, loaiPhiId, thang, nam);
            if (prevRecord.isPresent()) {
                chiSoThangTruoc = prevRecord.get().getChiSoMoi();
            } else {
                // Đây là tháng đầu tiên ghi chỉ số -> tiêu thụ = chỉ số mới (bắt đầu từ 0)
                chiSoThangTruoc = 0;
            }
        }
        
        return Math.max(0, chiSoHienTai - chiSoThangTruoc);
    }
}
