package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.DangKyTamTruDTO;
import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.entity.NhanKhau;
import com.nhom33.quanlychungcu.entity.TamTru;
import com.nhom33.quanlychungcu.exception.BadRequestException;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import com.nhom33.quanlychungcu.repository.NhanKhauRepository;
import com.nhom33.quanlychungcu.repository.TamTruRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service xử lý nghiệp vụ Đăng ký Tạm trú.
 * 
 * LOGIC NGHIỆP VỤ CHÍNH XÁC:
 * - Tạm trú = Người từ nơi khác đến ở tạm tại hộ gia đình.
 * - Người tạm trú BẮT BUỘC phải xuất hiện trong danh sách nhân khẩu của hộ.
 * - Luồng xử lý: Insert NhanKhau (TrangThai = "Tạm trú") -> Insert TamTru.
 * 
 * SERVICE NÀY CHỈ CHỨA:
 * - dangKyTamTru(): Đăng ký tạm trú (core business logic).
 * - Các hàm tra cứu/helper (findAll, getById, search...).
 * - KHÔNG có hàm create() độc lập (đã xóa vì sai logic).
 */
@Service
public class TamTruService {

    private static final Logger log = LoggerFactory.getLogger(TamTruService.class);

    /**
     * Trạng thái nhân khẩu khi đăng ký tạm trú.
     */
    private static final String TRANG_THAI_TAM_TRU = "Tạm trú";

    private final TamTruRepository tamTruRepo;
    private final NhanKhauRepository nhanKhauRepo;
    private final HoGiaDinhRepository hoGiaDinhRepo;

    public TamTruService(TamTruRepository tamTruRepo, 
                         NhanKhauRepository nhanKhauRepo,
                         HoGiaDinhRepository hoGiaDinhRepo) {
        this.tamTruRepo = tamTruRepo;
        this.nhanKhauRepo = nhanKhauRepo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
    }

    // ===================================================================
    //  CORE BUSINESS LOGIC: ĐĂNG KÝ TẠM TRÚ
    // ===================================================================

    /**
     * Đăng ký tạm trú cho người nơi khác đến ở.
     * 
     * QUY TRÌNH XỬ LÝ (Transaction):
     * 1. Validate input và kiểm tra hộ gia đình tồn tại.
     * 2. Kiểm tra CCCD chưa tồn tại trong hệ thống.
     * 3. INSERT NhanKhau với TrangThai = "Tạm trú".
     * 4. INSERT TamTru liên kết với NhanKhau vừa tạo.
     * 5. Cập nhật trạng thái hộ gia đình nếu cần.
     * 
     * @param dto DTO chứa thông tin đăng ký tạm trú
     * @return TamTru đã được lưu (kèm thông tin NhanKhau)
     * @throws ResourceNotFoundException nếu không tìm thấy hộ gia đình
     * @throws BadRequestException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public TamTru dangKyTamTru(@NonNull DangKyTamTruDTO dto) {
        log.info("Đăng ký tạm trú: {} (CCCD: {}) vào hộ ID: {}", 
                 dto.getHoTen(), dto.getSoCCCD(), dto.getHoGiaDinhId());

        // ===== BƯỚC 1: Kiểm tra hộ gia đình tồn tại =====
        HoGiaDinh hoGiaDinh = hoGiaDinhRepo.findById(dto.getHoGiaDinhId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy hộ gia đình với ID: " + dto.getHoGiaDinhId()
            ));

        // Kiểm tra trạng thái hộ gia đình
        String trangThaiHo = hoGiaDinh.getTrangThai();
        if ("Trống".equalsIgnoreCase(trangThaiHo) || "Không sử dụng".equalsIgnoreCase(trangThaiHo)) {
            throw new BadRequestException(
                "Hộ gia đình '" + hoGiaDinh.getMaHoGiaDinh() + "' đang ở trạng thái '" + 
                trangThaiHo + "'. Phải có người thường trú trước khi đăng ký tạm trú."
            );
        }

        // ===== BƯỚC 2: Kiểm tra CCCD chưa tồn tại =====
        if (nhanKhauRepo.existsBySoCCCD(dto.getSoCCCD())) {
            throw new BadRequestException(
                "Số CCCD '" + dto.getSoCCCD() + "' đã tồn tại trong hệ thống. " +
                "Nếu người này đã là nhân khẩu, không cần đăng ký tạm trú."
            );
        }

        // ===== BƯỚC 3: Validate ngày tháng =====
        validateDates(dto.getNgayBatDau(), dto.getNgayKetThuc());

        // ===== BƯỚC 4: INSERT NhanKhau với TrangThai = "Tạm trú" =====
        NhanKhau nhanKhau = new NhanKhau();
        nhanKhau.setHoGiaDinh(hoGiaDinh);
        nhanKhau.setHoTen(dto.getHoTen());
        nhanKhau.setSoCCCD(dto.getSoCCCD());
        nhanKhau.setNgaySinh(dto.getNgaySinh());
        nhanKhau.setGioiTinh(dto.getGioiTinh());
        nhanKhau.setSoDienThoai(dto.getSoDienThoai());
        nhanKhau.setEmail(dto.getEmail());
        nhanKhau.setQuanHeVoiChuHo(dto.getQuanHeVoiChuHo());
        nhanKhau.setLaChuHo(false); // Người tạm trú không thể là chủ hộ
        nhanKhau.setNgayChuyenDen(dto.getNgayBatDau());
        nhanKhau.setTrangThai(TRANG_THAI_TAM_TRU);

        NhanKhau savedNhanKhau = nhanKhauRepo.save(nhanKhau);
        log.info("Đã insert NhanKhau ID: {} với TrangThai = '{}'", 
                 savedNhanKhau.getId(), TRANG_THAI_TAM_TRU);

        // ===== BƯỚC 5: INSERT TamTru liên kết với NhanKhau =====
        TamTru tamTru = new TamTru();
        tamTru.setNhanKhau(savedNhanKhau);
        tamTru.setDiaChiThuongTru(dto.getDiaChiThuongTru());
        tamTru.setMaGiayTamTru(dto.getMaGiayTamTru());
        tamTru.setNgayBatDau(dto.getNgayBatDau());
        tamTru.setNgayKetThuc(dto.getNgayKetThuc());
        tamTru.setLyDo(dto.getLyDo());
        tamTru.setNgayDangKy(LocalDateTime.now());

        TamTru savedTamTru = tamTruRepo.save(tamTru);
        log.info("Đã insert TamTru ID: {} cho NhanKhau ID: {}", 
                 savedTamTru.getId(), savedNhanKhau.getId());

        // ===== BƯỚC 6: Cập nhật trạng thái hộ gia đình nếu cần =====
        // (Thực ra khi có tạm trú, hộ đã có người ở rồi)
        
        log.info("Đăng ký tạm trú thành công cho {} tại hộ {}", 
                 dto.getHoTen(), hoGiaDinh.getMaHoGiaDinh());

        return savedTamTru;
    }

    // ===================================================================
    //  HÀM HỦY TẠM TRÚ (KHI HẾT HẠN HOẶC RỜI ĐI)
    // ===================================================================

    /**
     * Hủy tạm trú (khi người tạm trú rời đi hoặc hết hạn).
     * 
     * Logic:
     * - Xóa record TamTru.
     * - Cập nhật NhanKhau.TrangThai = "Đã chuyển đi".
     * 
     * @param tamTruId ID bản ghi tạm trú
     */
    @Transactional
    public void huyTamTru(@NonNull Integer tamTruId) {
        TamTru tamTru = tamTruRepo.findById(tamTruId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy bản ghi tạm trú với ID: " + tamTruId
            ));

        NhanKhau nhanKhau = tamTru.getNhanKhau();
        
        // Cập nhật trạng thái nhân khẩu
        if (nhanKhau != null) {
            nhanKhau.setTrangThai("Đã chuyển đi");
            nhanKhauRepo.save(nhanKhau);
            log.info("Đã cập nhật NhanKhau {} sang trạng thái 'Đã chuyển đi'", nhanKhau.getId());
        }

        // Xóa record tạm trú
        tamTruRepo.delete(tamTru);
        log.info("Đã hủy tạm trú ID: {}", tamTruId);
    }

    // ===================================================================
    //  CÁC HÀM TRA CỨU / HELPER
    // ===================================================================

    /**
     * Lấy thông tin tạm trú theo ID.
     */
    public TamTru getById(@NonNull Integer id) {
        return tamTruRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy bản ghi tạm trú với ID: " + id
            ));
    }

    /**
     * Lấy tất cả bản ghi (có phân trang).
     */
    public Page<TamTru> findAll(@NonNull Pageable pageable) {
        return tamTruRepo.findAll(pageable);
    }

    /**
     * Tìm kiếm theo tên nhân khẩu (có phân trang).
     */
    public Page<TamTru> searchByNhanKhauName(String hoTen, @NonNull Pageable pageable) {
        if (hoTen == null || hoTen.isBlank()) {
            return tamTruRepo.findAll(pageable);
        }
        return tamTruRepo.findByNhanKhau_HoTenContainingIgnoreCase(hoTen, pageable);
    }

    /**
     * Tìm các bản ghi tạm trú theo hộ gia đình.
     */
    public Page<TamTru> findByHoGiaDinhId(@NonNull Integer hoGiaDinhId, @NonNull Pageable pageable) {
        return tamTruRepo.findByNhanKhau_HoGiaDinh_Id(hoGiaDinhId, pageable);
    }

    /**
     * Xóa bản ghi tạm trú (không cập nhật trạng thái nhân khẩu).
     * Sử dụng huyTamTru() nếu muốn cập nhật trạng thái.
     */
    @Transactional
    public void delete(@NonNull Integer id) {
        if (!tamTruRepo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy bản ghi tạm trú với ID: " + id);
        }
        tamTruRepo.deleteById(id);
    }

    // ===================================================================
    //  PRIVATE HELPER METHODS
    // ===================================================================

    /**
     * Validate ngày bắt đầu và ngày kết thúc.
     */
    private void validateDates(LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        if (ngayBatDau == null) {
            throw new BadRequestException("Ngày bắt đầu tạm trú không được để trống");
        }
        
        if (ngayKetThuc != null && ngayKetThuc.isBefore(ngayBatDau)) {
            throw new BadRequestException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }
    }
}
