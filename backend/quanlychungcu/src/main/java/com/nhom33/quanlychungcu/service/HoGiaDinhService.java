package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.HoGiaDinhRequestDTO;
import com.nhom33.quanlychungcu.entity.ChiSoDienNuoc;
import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.entity.LoaiPhi;
import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.exception.BadRequestException;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.ChiSoDienNuocRepository;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import com.nhom33.quanlychungcu.repository.LoaiPhiRepository;
import com.nhom33.quanlychungcu.repository.NhanKhauRepository;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class HoGiaDinhService {

    private static final Logger log = LoggerFactory.getLogger(HoGiaDinhService.class);

    /**
     * Giá trị mặc định cho TenChuHo khi hộ gia đình chưa có chủ hộ.
     */
    public static final String DEFAULT_CHU_HO_NAME = "Chưa có chủ hộ";

    private final HoGiaDinhRepository repo;
    private final ToaNhaRepository toaNhaRepo;
    private final NhanKhauRepository nhanKhauRepo;
    private final LoaiPhiRepository loaiPhiRepo;
    private final ChiSoDienNuocRepository chiSoRepo;
    
    @PersistenceContext
    private EntityManager entityManager;

    public HoGiaDinhService(HoGiaDinhRepository repo, 
                           ToaNhaRepository toaNhaRepo, 
                           NhanKhauRepository nhanKhauRepo,
                           LoaiPhiRepository loaiPhiRepo,
                           ChiSoDienNuocRepository chiSoRepo) {
        this.repo = repo;
        this.toaNhaRepo = toaNhaRepo;
        this.nhanKhauRepo = nhanKhauRepo;
        this.loaiPhiRepo = loaiPhiRepo;
        this.chiSoRepo = chiSoRepo;
    }

    /**
     * Tạo mới Hộ gia đình (căn hộ rỗng - chưa có chủ hộ).
     * 
     * LUỒNG NGHIỆP VỤ MỚI:
     * 1. Chỉ nhận thông tin vật lý của căn hộ (MaHoGiaDinh, SoTang, SoCanHo, DienTich, ID_ToaNha).
     * 2. TenChuHo sẽ được set mặc định là "Chưa có chủ hộ".
     * 3. TrangThai mặc định là "Trống" (vì chưa có ai ở).
     * 4. Khi thêm nhân khẩu có QuanHeVoiChuHo = "Chủ hộ", TenChuHo sẽ được tự động cập nhật.
     * 
     * Unique constraint: (MaHoGiaDinh, ID_ToaNha) phải là duy nhất.
     * 
     * @param dto Thông tin căn hộ
     * @return Hộ gia đình đã tạo
     */
    @Transactional
    public HoGiaDinh createEmptyHousehold(HoGiaDinhRequestDTO dto) {
        log.info("Bắt đầu tạo hộ gia đình rỗng: {}", dto.getMaHoGiaDinh());

        // === Bước 1: Validate ToaNha ===
        ToaNha toaNha = toaNhaRepo.findById(dto.getIdToaNha())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy tòa nhà với ID: " + dto.getIdToaNha()
            ));

        // === Bước 2: Validate unique constraint (MaHoGiaDinh, ID_ToaNha) ===
        if (repo.existsByMaHoGiaDinhAndToaNhaId(dto.getMaHoGiaDinh(), dto.getIdToaNha())) {
            throw new BadRequestException(
                "Mã hộ gia đình '" + dto.getMaHoGiaDinh() + 
                "' đã tồn tại trong tòa nhà '" + toaNha.getTenToaNha() + "'"
            );
        }

        // === Bước 3: Tạo entity HoGiaDinh ===
        HoGiaDinh hoGiaDinh = new HoGiaDinh();
        hoGiaDinh.setMaHoGiaDinh(dto.getMaHoGiaDinh());
        hoGiaDinh.setToaNha(toaNha);
        hoGiaDinh.setSoCanHo(dto.getSoCanHo());
        hoGiaDinh.setSoTang(dto.getSoTang());
        hoGiaDinh.setDienTich(dto.getDienTich());
        
        // TenChuHo mặc định - sẽ được cập nhật khi thêm nhân khẩu là chủ hộ
        hoGiaDinh.setTenChuHo(DEFAULT_CHU_HO_NAME);
        
        // TrangThai mặc định là "Trống" vì chưa có ai ở
        hoGiaDinh.setTrangThai("Trống");

        // Lưu và return
        HoGiaDinh savedHoGiaDinh = repo.save(hoGiaDinh);

        // === Bước 4: Tạo chỉ số điện/nước bàn giao (nếu có) ===
        createInitialMeterReadings(savedHoGiaDinh, dto);

        log.info("Tạo thành công hộ gia đình rỗng: {} (ID: {})", 
                 savedHoGiaDinh.getMaHoGiaDinh(), savedHoGiaDinh.getId());

        return savedHoGiaDinh;
    }

    /**
     * Tạo chỉ số điện/nước bàn giao cho hộ gia đình mới.
     * 
     * Chỉ số bàn giao được lưu như chỉ số của tháng trước (tháng hiện tại - 1).
     * Ví dụ: Tạo hộ gia đình tháng 2/2026 với chỉ số điện bàn giao = 100
     *        → Tạo bản ghi ChiSoDienNuoc cho tháng 1/2026 với ChiSoMoi = 100
     * 
     * Khi ghi chỉ số tháng 2/2026, hệ thống sẽ dùng 100 làm chỉ số tháng trước để tính tiêu thụ.
     * 
     * @param hoGiaDinh Hộ gia đình vừa tạo
     * @param dto DTO chứa chỉ số bàn giao
     */
    private void createInitialMeterReadings(HoGiaDinh hoGiaDinh, HoGiaDinhRequestDTO dto) {
        // Tính tháng/năm trước (để lưu chỉ số bàn giao)
        LocalDate now = LocalDate.now();
        int thangTruoc = now.getMonthValue() == 1 ? 12 : now.getMonthValue() - 1;
        int namTruoc = now.getMonthValue() == 1 ? now.getYear() - 1 : now.getYear();

        // === Tạo chỉ số điện bàn giao ===
        if (dto.getChiSoDienBanGiao() != null && dto.getChiSoDienBanGiao() >= 0) {
            LoaiPhi loaiPhiDien = loaiPhiRepo.findFirstByTenLoaiPhi("Điện")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí 'Điện' trong hệ thống"));

            ChiSoDienNuoc chiSoDien = new ChiSoDienNuoc();
            chiSoDien.setHoGiaDinh(hoGiaDinh);
            chiSoDien.setLoaiPhi(loaiPhiDien);
            chiSoDien.setThang(thangTruoc);
            chiSoDien.setNam(namTruoc);
            chiSoDien.setChiSoMoi(dto.getChiSoDienBanGiao());
            
            chiSoRepo.save(chiSoDien);
            log.info("Tạo chỉ số điện bàn giao cho hộ {}: {} (tháng {}/{})", 
                     hoGiaDinh.getMaHoGiaDinh(), dto.getChiSoDienBanGiao(), thangTruoc, namTruoc);
        }

        // === Tạo chỉ số nước bàn giao ===
        if (dto.getChiSoNuocBanGiao() != null && dto.getChiSoNuocBanGiao() >= 0) {
            LoaiPhi loaiPhiNuoc = loaiPhiRepo.findFirstByTenLoaiPhi("Nước")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí 'Nước' trong hệ thống"));

            ChiSoDienNuoc chiSoNuoc = new ChiSoDienNuoc();
            chiSoNuoc.setHoGiaDinh(hoGiaDinh);
            chiSoNuoc.setLoaiPhi(loaiPhiNuoc);
            chiSoNuoc.setThang(thangTruoc);
            chiSoNuoc.setNam(namTruoc);
            chiSoNuoc.setChiSoMoi(dto.getChiSoNuocBanGiao());
            
            chiSoRepo.save(chiSoNuoc);
            log.info("Tạo chỉ số nước bàn giao cho hộ {}: {} (tháng {}/{})", 
                     hoGiaDinh.getMaHoGiaDinh(), dto.getChiSoNuocBanGiao(), thangTruoc, namTruoc);
        }
    }

    /**
     * Tạo mới hộ gia đình (API legacy - từ Entity).
     * Khuyến khích sử dụng createEmptyHousehold(DTO) thay thế.
     */
    @Transactional
    public HoGiaDinh create(HoGiaDinh hoGiaDinh) {
        // Bắt buộc phải có ToaNha
        if (hoGiaDinh.getToaNha() == null || hoGiaDinh.getToaNha().getId() == null) {
            throw new IllegalArgumentException("Tòa nhà không được để trống. Vui lòng chọn tòa nhà.");
        }

        // Xử lý ToaNha - validate và load entity
        ToaNha toaNha = toaNhaRepo.findById(hoGiaDinh.getToaNha().getId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy tòa nhà với ID: " + hoGiaDinh.getToaNha().getId()
            ));

        // Kiểm tra unique constraint (MaHoGiaDinh, ID_ToaNha)
        if (repo.existsByMaHoGiaDinhAndToaNhaId(hoGiaDinh.getMaHoGiaDinh(), toaNha.getId())) {
            throw new BadRequestException(
                "Mã hộ gia đình '" + hoGiaDinh.getMaHoGiaDinh() + 
                "' đã tồn tại trong tòa nhà '" + toaNha.getTenToaNha() + "'"
            );
        }

        hoGiaDinh.setToaNha(toaNha);

        // Set ngày tạo sẽ được xử lý bởi @PrePersist
        return repo.save(hoGiaDinh);
    }

    @Transactional
    public HoGiaDinh update(@NonNull Integer id, HoGiaDinh updated) {
        HoGiaDinh exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + id));

        // Cập nhật ToaNha (bắt buộc) - validate trước
        if (updated.getToaNha() == null || updated.getToaNha().getId() == null) {
            throw new IllegalArgumentException("Tòa nhà không được để trống. Vui lòng chọn tòa nhà.");
        }
        
        ToaNha toaNha = toaNhaRepo.findById(updated.getToaNha().getId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy tòa nhà với ID: " + updated.getToaNha().getId()
            ));

        // Kiểm tra unique constraint (MaHoGiaDinh, ID_ToaNha) - loại trừ bản ghi hiện tại
        boolean isDuplicatePair = repo.existsByMaHoGiaDinhAndToaNhaIdExcludingId(
                updated.getMaHoGiaDinh(), 
                toaNha.getId(), 
                id
        );
        if (isDuplicatePair) {
            throw new BadRequestException(
                "Mã hộ gia đình '" + updated.getMaHoGiaDinh() + 
                "' đã tồn tại trong tòa nhà '" + toaNha.getTenToaNha() + "'"
            );
        }

        // Cập nhật thông tin
        exist.setMaHoGiaDinh(updated.getMaHoGiaDinh());
        exist.setTenChuHo(updated.getTenChuHo());
        exist.setSoDienThoaiLienHe(updated.getSoDienThoaiLienHe());
        exist.setEmailLienHe(updated.getEmailLienHe());
        exist.setSoTang(updated.getSoTang());
        exist.setSoCanHo(updated.getSoCanHo());
        exist.setDienTich(updated.getDienTich());
        exist.setTrangThai(updated.getTrangThai());
        exist.setToaNha(toaNha);

        // NgayCapNhat sẽ được set bởi @PreUpdate
        return repo.save(exist);
    }

    /**
     * Hard Delete: Xóa vĩnh viễn hộ gia đình và tất cả dữ liệu liên quan.
     * 
     * Cascade Delete sẽ xóa:
     * - NhanKhau (nhân khẩu)
     * - TamTru (đăng ký tạm trú)
     * - HoaDon (hóa đơn) -> ChiTietHoaDon, LichSuThanhToan
     * - DinhMucThu (định mức thu)
     * - PhanAnh (phản ánh) -> PhanHoi
     * 
     * @param id ID của hộ gia đình cần xóa
     * @throws ResourceNotFoundException nếu không tìm thấy hộ gia đình
     * @throws BadRequestException nếu có lỗi nghiệp vụ
     */
    @Transactional
    public void delete(@NonNull Integer id) {
        log.info("Bắt đầu xóa hộ gia đình với ID: {}", id);
        
        // Bước 1: Tìm entity (không dùng existsById vì cần load entity để cascade delete)
        HoGiaDinh hoGiaDinh = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + id));
        
        String maHoGiaDinh = hoGiaDinh.getMaHoGiaDinh();
        log.info("Tìm thấy hộ gia đình: {} - Số nhân khẩu: {}", 
                 maHoGiaDinh, hoGiaDinh.getDanhSachNhanKhau().size());
        
        // Bước 2: Xóa entity (CascadeType.ALL + orphanRemoval sẽ xóa tất cả entity con)
        try {
            repo.delete(hoGiaDinh);
            
            // Bước 3: Flush để đảm bảo SQL DELETE được thực thi ngay lập tức
            entityManager.flush();
            
            // Bước 4: Clear cache để tránh stale data
            entityManager.clear();
            
            log.info("Xóa thành công hộ gia đình: {}", maHoGiaDinh);
        } catch (Exception e) {
            log.error("Lỗi khi xóa hộ gia đình {}: {}", maHoGiaDinh, e.getMessage());
            throw new BadRequestException(
                "Không thể xóa hộ gia đình '" + maHoGiaDinh + "'. " +
                "Có thể do dữ liệu liên quan đang được sử dụng bởi chức năng khác. " +
                "Chi tiết: " + e.getMessage()
            );
        }
    }

    public HoGiaDinh getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + id));
    }

    /**
     * Lấy chi tiết hộ gia đình kèm danh sách nhân khẩu
     * Fix LazyLoading bằng cách khởi tạo collection trong transaction
     */
    @Transactional(readOnly = true)
    public HoGiaDinh getDetail(@NonNull Integer id) {
        HoGiaDinh hoGiaDinh = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + id));
        
        // Force initialization of the lazy-loaded collection within the transaction
        hoGiaDinh.getDanhSachNhanKhau().size();
        
        return hoGiaDinh;
    }

    public HoGiaDinh getByMaHoGiaDinh(String maHoGiaDinh) {
        return repo.findByMaHoGiaDinh(maHoGiaDinh)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với mã: " + maHoGiaDinh));
    }

    public Page<HoGiaDinh> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<HoGiaDinh> searchByTenChuHo(String tenChuHo, @NonNull Pageable pageable) {
        if (tenChuHo == null || tenChuHo.isBlank()) {
            return repo.findAll(pageable);
        }
        return repo.findByTenChuHoContainingIgnoreCase(tenChuHo, pageable);
    }

    public Page<HoGiaDinh> searchBySoCanHo(String soCanHo, @NonNull Pageable pageable) {
        return repo.findBySoCanHo(soCanHo, pageable);
    }

    public Page<HoGiaDinh> searchByTang(Integer soTang, @NonNull Pageable pageable) {
        return repo.findBySoTang(soTang, pageable);
    }

    public Page<HoGiaDinh> searchByTrangThai(String trangThai, @NonNull Pageable pageable) {
        return repo.findByTrangThai(trangThai, pageable);
    }

    public Page<HoGiaDinh> search(String maHoGiaDinh, String tenChuHo, String soCanHo, 
                                   String trangThai, @NonNull Pageable pageable) {
        return repo.search(maHoGiaDinh, tenChuHo, soCanHo, trangThai, pageable);
    }

    public long countByTrangThai(String trangThai) {
        return repo.countByTrangThai(trangThai);
    }
}
