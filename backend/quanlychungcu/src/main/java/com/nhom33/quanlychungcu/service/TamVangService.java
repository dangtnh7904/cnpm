package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.DangKyTamVangDTO;
import com.nhom33.quanlychungcu.entity.NhanKhau;
import com.nhom33.quanlychungcu.entity.TamVang;
import com.nhom33.quanlychungcu.exception.BadRequestException;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.NhanKhauRepository;
import com.nhom33.quanlychungcu.repository.TamVangRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Service xử lý nghiệp vụ Đăng ký Tạm vắng.
 * 
 * LOGIC NGHIỆP VỤ CHÍNH XÁC:
 * - Tạm vắng = Người ĐÃ LÀ THÀNH VIÊN của hộ đi vắng tạm thời.
 * - KHÔNG thêm mới nhân khẩu, chỉ CẬP NHẬT trạng thái.
 * - Luồng xử lý: Validate NhanKhau -> Update TrangThai -> Insert TamVang.
 * 
 * SERVICE NÀY CHỈ CHỨA:
 * - dangKyTamVang(): Đăng ký tạm vắng (core business logic).
 * - ketThucTamVang(): Kết thúc tạm vắng (khôi phục trạng thái).
 * - Các hàm tra cứu/helper.
 * - KHÔNG có hàm create() độc lập (đã xóa vì sai logic).
 */
@Service
public class TamVangService {

    private static final Logger log = LoggerFactory.getLogger(TamVangService.class);

    /**
     * Các trạng thái được phép đăng ký tạm vắng.
     */
    private static final Set<String> ALLOWED_STATUSES = Set.of(
        "Đang ở", "Thường trú", "Hoat dong", "Hoạt động"
    );
    
    /**
     * Các trạng thái KHÔNG được phép đăng ký tạm vắng.
     */
    private static final Set<String> BLOCKED_STATUSES = Set.of(
        "Tạm vắng", "Tam vang", 
        "Tạm trú", "Tam tru",
        "Đã chuyển đi", "Da chuyen di", 
        "Đã mất", "Da mat"
    );

    /**
     * Trạng thái sau khi đăng ký tạm vắng.
     */
    private static final String TRANG_THAI_TAM_VANG = "Tạm vắng";

    /**
     * Trạng thái sau khi kết thúc tạm vắng (trở về).
     */
    private static final String TRANG_THAI_DANG_O = "Đang ở";

    private final TamVangRepository tamVangRepo;
    private final NhanKhauRepository nhanKhauRepo;

    public TamVangService(TamVangRepository tamVangRepo, NhanKhauRepository nhanKhauRepo) {
        this.tamVangRepo = tamVangRepo;
        this.nhanKhauRepo = nhanKhauRepo;
    }

    // ===================================================================
    //  CORE BUSINESS LOGIC: ĐĂNG KÝ TẠM VẮNG
    // ===================================================================

    /**
     * Đăng ký tạm vắng cho nhân khẩu (người ở đây đi vắng).
     * 
     * QUY TRÌNH XỬ LÝ (Transaction):
     * 1. Kiểm tra NhanKhau tồn tại.
     * 2. Kiểm tra trạng thái hiện tại: Phải là "Đang ở"/"Thường trú".
     * 3. UPDATE NhanKhau.TrangThai = "Tạm vắng".
     * 4. INSERT TamVang liên kết với NhanKhau.
     * 
     * @param dto DTO chứa thông tin đăng ký tạm vắng
     * @return TamVang đã được lưu
     * @throws ResourceNotFoundException nếu không tìm thấy nhân khẩu
     * @throws BadRequestException nếu nhân khẩu không ở trạng thái được phép
     */
    @Transactional
    public TamVang dangKyTamVang(@NonNull DangKyTamVangDTO dto) {
        log.info("Đăng ký tạm vắng cho NhanKhau ID: {}", dto.getNhanKhauId());

        // ===== BƯỚC 1: Kiểm tra nhân khẩu tồn tại =====
        NhanKhau nhanKhau = nhanKhauRepo.findById(dto.getNhanKhauId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy nhân khẩu với ID: " + dto.getNhanKhauId()
            ));

        // ===== BƯỚC 2: Kiểm tra trạng thái nhân khẩu =====
        String trangThaiHienTai = nhanKhau.getTrangThai();
        
        if (trangThaiHienTai != null && !trangThaiHienTai.isBlank()) {
            String normalizedStatus = trangThaiHienTai.trim();
            
            // Kiểm tra nếu đang ở trạng thái bị chặn
            if (BLOCKED_STATUSES.stream().anyMatch(s -> s.equalsIgnoreCase(normalizedStatus))) {
                throw new BadRequestException(
                    "Nhân khẩu '" + nhanKhau.getHoTen() + "' đang ở trạng thái '" + trangThaiHienTai + 
                    "', không thể đăng ký tạm vắng. " +
                    "Chỉ nhân khẩu có trạng thái 'Đang ở' hoặc 'Thường trú' mới được đăng ký."
                );
            }
        }

        // ===== BƯỚC 3: Validate ngày tháng =====
        validateDates(dto.getNgayDi(), dto.getNgayVe());

        // ===== BƯỚC 4: UPDATE trạng thái nhân khẩu =====
        nhanKhau.setTrangThai(TRANG_THAI_TAM_VANG);
        nhanKhauRepo.save(nhanKhau);
        log.info("Đã cập nhật NhanKhau {} sang trạng thái '{}'", 
                 nhanKhau.getId(), TRANG_THAI_TAM_VANG);

        // ===== BƯỚC 5: INSERT TamVang =====
        TamVang tamVang = new TamVang();
        tamVang.setNhanKhau(nhanKhau);
        tamVang.setNgayBatDau(dto.getNgayDi());
        tamVang.setNgayKetThuc(dto.getNgayVe());
        tamVang.setLyDo(dto.getLyDo());
        tamVang.setNoiDen(dto.getNoiDen());
        tamVang.setNgayDangKy(LocalDateTime.now());

        TamVang saved = tamVangRepo.save(tamVang);
        log.info("Đã insert TamVang ID: {} cho NhanKhau {}", 
                 saved.getId(), nhanKhau.getHoTen());

        return saved;
    }

    // ===================================================================
    //  KẾT THÚC TẠM VẮNG (TRỞ VỀ)
    // ===================================================================

    /**
     * Kết thúc tạm vắng (nhân khẩu đã trở về).
     * 
     * Logic:
     * - Cập nhật NhanKhau.TrangThai = "Đang ở".
     * - Giữ lại record TamVang để lịch sử (hoặc xóa tùy yêu cầu).
     * 
     * @param tamVangId ID bản ghi tạm vắng
     */
    @Transactional
    public void ketThucTamVang(@NonNull Integer tamVangId) {
        TamVang tamVang = tamVangRepo.findById(tamVangId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy bản ghi tạm vắng với ID: " + tamVangId
            ));

        NhanKhau nhanKhau = tamVang.getNhanKhau();
        
        if (nhanKhau != null) {
            // Kiểm tra còn bản ghi tạm vắng nào khác đang active không
            long activeCount = tamVangRepo.countByNhanKhauIdAndNgayKetThucAfter(
                nhanKhau.getId(), LocalDate.now()
            );
            
            // Nếu đây là bản ghi tạm vắng duy nhất hoặc cuối cùng
            if (activeCount <= 1) {
                nhanKhau.setTrangThai(TRANG_THAI_DANG_O);
                nhanKhauRepo.save(nhanKhau);
                log.info("Đã khôi phục NhanKhau {} sang trạng thái '{}'", 
                         nhanKhau.getId(), TRANG_THAI_DANG_O);
            }
        }

        // Cập nhật ngày kết thúc thực tế
        tamVang.setNgayKetThuc(LocalDate.now());
        tamVangRepo.save(tamVang);
        log.info("Đã kết thúc tạm vắng ID: {}", tamVangId);
    }

    // ===================================================================
    //  CÁC HÀM TRA CỨU / HELPER
    // ===================================================================

    /**
     * Lấy thông tin tạm vắng theo ID.
     */
    public TamVang getById(@NonNull Integer id) {
        return tamVangRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy bản ghi tạm vắng với ID: " + id
            ));
    }

    /**
     * Lấy tất cả bản ghi (có phân trang).
     */
    public Page<TamVang> findAll(@NonNull Pageable pageable) {
        return tamVangRepo.findAll(pageable);
    }

    /**
     * Tìm kiếm theo nơi đến (có phân trang).
     */
    public Page<TamVang> searchByNoiDen(String noiDen, @NonNull Pageable pageable) {
        if (noiDen == null || noiDen.isBlank()) {
            return tamVangRepo.findAll(pageable);
        }
        return tamVangRepo.findByNoiDenContainingIgnoreCase(noiDen, pageable);
    }

    /**
     * Tìm các bản ghi tạm vắng theo nhân khẩu.
     */
    public Page<TamVang> findByNhanKhauId(@NonNull Integer nhanKhauId, @NonNull Pageable pageable) {
        return tamVangRepo.findByNhanKhauId(nhanKhauId, pageable);
    }

    /**
     * Tìm các bản ghi tạm vắng theo hộ gia đình.
     */
    public Page<TamVang> findByHoGiaDinhId(@NonNull Integer hoGiaDinhId, @NonNull Pageable pageable) {
        return tamVangRepo.findByNhanKhauHoGiaDinhId(hoGiaDinhId, pageable);
    }

    /**
     * Xóa bản ghi tạm vắng và khôi phục trạng thái nhân khẩu.
     */
    @Transactional
    public void delete(@NonNull Integer id) {
        TamVang tamVang = tamVangRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy bản ghi tạm vắng với ID: " + id
            ));

        NhanKhau nhanKhau = tamVang.getNhanKhau();
        
        // Khôi phục trạng thái nếu cần
        if (nhanKhau != null && TRANG_THAI_TAM_VANG.equalsIgnoreCase(nhanKhau.getTrangThai())) {
            // Kiểm tra còn bản ghi tạm vắng nào khác không
            long remainingCount = tamVangRepo.countByNhanKhauId(nhanKhau.getId());
            if (remainingCount <= 1) {
                nhanKhau.setTrangThai(TRANG_THAI_DANG_O);
                nhanKhauRepo.save(nhanKhau);
                log.info("Đã khôi phục NhanKhau {} sang trạng thái '{}'", 
                         nhanKhau.getId(), TRANG_THAI_DANG_O);
            }
        }

        tamVangRepo.deleteById(id);
        log.info("Đã xóa tạm vắng ID: {}", id);
    }

    // ===================================================================
    //  PRIVATE HELPER METHODS
    // ===================================================================

    /**
     * Validate ngày đi và ngày về.
     */
    private void validateDates(LocalDate ngayDi, LocalDate ngayVe) {
        if (ngayDi == null) {
            throw new BadRequestException("Ngày đi không được để trống");
        }
        
        if (ngayVe != null && ngayVe.isBefore(ngayDi)) {
            throw new BadRequestException("Ngày về phải sau hoặc bằng ngày đi");
        }
    }
}
