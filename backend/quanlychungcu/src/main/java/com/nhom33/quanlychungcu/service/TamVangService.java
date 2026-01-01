package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.DangKyTamVangDTO;
import com.nhom33.quanlychungcu.entity.NhanKhau;
import com.nhom33.quanlychungcu.entity.TamVang;
import com.nhom33.quanlychungcu.exception.BadRequestException;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.NhanKhauRepository;
import com.nhom33.quanlychungcu.repository.TamVangRepository;
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
 * Quy tắc nghiệp vụ:
 * - Chỉ nhân khẩu đang cư trú (trạng thái "Đang ở", "Thường trú", "Hoat dong") mới được đăng ký tạm vắng.
 * - Nhân khẩu đã "Tạm vắng", "Đã chuyển đi", "Đã mất" không được đăng ký thêm.
 * - Sau khi đăng ký, trạng thái nhân khẩu tự động cập nhật thành "Tạm vắng".
 */
@Service
public class TamVangService {

    // Các trạng thái được phép đăng ký tạm vắng
    private static final Set<String> ALLOWED_STATUSES = Set.of(
        "Đang ở", "Thường trú", "Hoat dong", "Hoạt động"
    );
    
    // Các trạng thái KHÔNG được phép đăng ký tạm vắng
    private static final Set<String> BLOCKED_STATUSES = Set.of(
        "Tạm vắng", "Tam vang", "Đã chuyển đi", "Da chuyen di", "Đã mất", "Da mat"
    );

    private final TamVangRepository repo;
    private final NhanKhauRepository nhanKhauRepo;

    public TamVangService(TamVangRepository repo, NhanKhauRepository nhanKhauRepo) {
        this.repo = repo;
        this.nhanKhauRepo = nhanKhauRepo;
    }

    /**
     * Đăng ký tạm vắng sử dụng DTO (API mới - khuyến khích sử dụng).
     * 
     * @param dto DTO chứa thông tin đăng ký tạm vắng
     * @return TamVang đã được lưu
     * @throws ResourceNotFoundException nếu không tìm thấy nhân khẩu
     * @throws BadRequestException nếu nhân khẩu không ở trạng thái được phép
     */
    @Transactional
    public TamVang dangKyTamVang(DangKyTamVangDTO dto) {
        // === Bước 1: Kiểm tra nhân khẩu tồn tại ===
        NhanKhau nhanKhau = nhanKhauRepo.findById(dto.getNhanKhauId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy nhân khẩu với ID: " + dto.getNhanKhauId()
            ));
        
        // === Bước 2: Kiểm tra trạng thái nhân khẩu ===
        String trangThai = nhanKhau.getTrangThai();
        if (trangThai != null && !trangThai.isBlank()) {
            String normalizedStatus = trangThai.trim();
            
            if (BLOCKED_STATUSES.stream().anyMatch(s -> s.equalsIgnoreCase(normalizedStatus))) {
                throw new BadRequestException(
                    "Nhân khẩu '" + nhanKhau.getHoTen() + "' đang ở trạng thái '" + trangThai + 
                    "', không thể đăng ký tạm vắng. Chỉ nhân khẩu đang cư trú mới được đăng ký."
                );
            }
        }
        
        // === Bước 3: Validate ngày tháng ===
        validateDates(dto.getNgayDi(), dto.getNgayVe());
        
        // === Bước 4: Tạo entity TamVang từ DTO ===
        TamVang tamVang = new TamVang();
        tamVang.setNhanKhau(nhanKhau);
        tamVang.setNgayBatDau(dto.getNgayDi());
        tamVang.setNgayKetThuc(dto.getNgayVe());
        tamVang.setLyDo(dto.getLyDo());
        tamVang.setNoiDen(dto.getNoiDen());
        tamVang.setNgayDangKy(LocalDateTime.now());
        
        TamVang saved = repo.save(tamVang);
        
        // === Bước 5: Cập nhật trạng thái nhân khẩu thành "Tạm vắng" ===
        nhanKhau.setTrangThai("Tạm vắng");
        nhanKhauRepo.save(nhanKhau);
        
        return saved;
    }

    /**
     * Đăng ký tạm vắng cho nhân khẩu (API legacy).
     * Khuyến khích sử dụng dangKyTamVang(DangKyTamVangDTO) thay thế.
     * 
     * @param tamVang Entity chứa thông tin đăng ký (bao gồm nhanKhau với id)
     * @return TamVang đã được lưu
     * @throws ResourceNotFoundException nếu không tìm thấy nhân khẩu
     * @throws BadRequestException nếu nhân khẩu không ở trạng thái được phép
     */
    @Transactional
    public TamVang create(TamVang tamVang) {
        // === Bước 1: Validate input cơ bản ===
        if (tamVang.getNhanKhau() == null || tamVang.getNhanKhau().getId() == null) {
            throw new BadRequestException("Thông tin nhân khẩu không được để trống");
        }
        
        Integer nhanKhauId = tamVang.getNhanKhau().getId();
        
        // === Bước 2: Kiểm tra nhân khẩu tồn tại ===
        NhanKhau nhanKhau = nhanKhauRepo.findById(nhanKhauId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy nhân khẩu với ID: " + nhanKhauId
            ));
        
        // === Bước 3: Kiểm tra trạng thái nhân khẩu ===
        String trangThai = nhanKhau.getTrangThai();
        
        // Nếu trạng thái null hoặc rỗng, coi như chưa xác định -> cho phép
        if (trangThai != null && !trangThai.isBlank()) {
            String normalizedStatus = trangThai.trim();
            
            // Kiểm tra nếu đang ở trạng thái bị chặn
            if (BLOCKED_STATUSES.stream().anyMatch(s -> s.equalsIgnoreCase(normalizedStatus))) {
                throw new BadRequestException(
                    "Nhân khẩu '" + nhanKhau.getHoTen() + "' đang ở trạng thái '" + trangThai + 
                    "', không thể đăng ký tạm vắng. Chỉ nhân khẩu đang cư trú mới được đăng ký."
                );
            }
            
            // Nếu không nằm trong danh sách cho phép -> cảnh báo nhưng vẫn cho phép (trạng thái mới)
            // Có thể bỏ block này nếu muốn strict hơn
        }
        
        // === Bước 4: Validate ngày tháng ===
        validateDates(tamVang.getNgayBatDau(), tamVang.getNgayKetThuc());
        
        // === Bước 5: Gán entity NhanKhau đầy đủ và lưu ===
        tamVang.setNhanKhau(nhanKhau);
        
        // Đảm bảo ngày đăng ký được set (có thể để @PrePersist xử lý)
        if (tamVang.getNgayDangKy() == null) {
            tamVang.setNgayDangKy(java.time.LocalDateTime.now());
        }
        
        TamVang saved = repo.save(tamVang);
        
        // === Bước 6: Cập nhật trạng thái nhân khẩu thành "Tạm vắng" ===
        nhanKhau.setTrangThai("Tạm vắng");
        nhanKhauRepo.save(nhanKhau);
        
        return saved;
    }

    /**
     * Cập nhật thông tin tạm vắng.
     * Chỉ cho phép cập nhật: ngày bắt đầu, ngày kết thúc, nơi đến, lý do.
     * KHÔNG cho phép đổi nhân khẩu.
     */
    @Transactional
    public TamVang update(@NonNull Integer id, TamVang updated) {
        TamVang exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tạm vắng với ID: " + id));
        
        // Validate ngày tháng nếu có thay đổi
        LocalDate ngayBatDau = updated.getNgayBatDau() != null ? updated.getNgayBatDau() : exist.getNgayBatDau();
        LocalDate ngayKetThuc = updated.getNgayKetThuc() != null ? updated.getNgayKetThuc() : exist.getNgayKetThuc();
        validateDates(ngayBatDau, ngayKetThuc);
        
        // Cập nhật các trường được phép
        exist.setNgayBatDau(ngayBatDau);
        exist.setNgayKetThuc(ngayKetThuc);
        if (updated.getNoiDen() != null) exist.setNoiDen(updated.getNoiDen());
        if (updated.getLyDo() != null) exist.setLyDo(updated.getLyDo());
        
        return repo.save(exist);
    }

    /**
     * Xóa bản ghi tạm vắng.
     * Lưu ý: Có thể cần khôi phục trạng thái nhân khẩu về "Đang ở".
     */
    @Transactional
    public void delete(@NonNull Integer id) {
        TamVang tamVang = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tạm vắng với ID: " + id));
        
        // Khôi phục trạng thái nhân khẩu nếu họ chỉ có 1 bản ghi tạm vắng
        NhanKhau nhanKhau = tamVang.getNhanKhau();
        if (nhanKhau != null) {
            // Kiểm tra xem còn bản ghi tạm vắng nào khác không
            long remainingCount = repo.countByNhanKhauId(nhanKhau.getId());
            if (remainingCount <= 1) {
                // Đây là bản ghi cuối cùng -> khôi phục trạng thái
                nhanKhau.setTrangThai("Đang ở");
                nhanKhauRepo.save(nhanKhau);
            }
        }
        
        repo.deleteById(id);
    }

    /**
     * Lấy thông tin tạm vắng theo ID.
     */
    public TamVang getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tạm vắng với ID: " + id));
    }

    /**
     * Tìm kiếm theo nơi đến (có phân trang).
     */
    public Page<TamVang> searchByNoiDen(String noiDen, @NonNull Pageable pageable) {
        if (noiDen == null || noiDen.isBlank()) {
            return repo.findAll(pageable);
        }
        return repo.findByNoiDenContainingIgnoreCase(noiDen, pageable);
    }

    // ===== Private Helper Methods =====
    
    /**
     * Validate ngày bắt đầu và ngày kết thúc.
     */
    private void validateDates(LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        if (ngayBatDau == null || ngayKetThuc == null) {
            throw new BadRequestException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        
        if (ngayKetThuc.isBefore(ngayBatDau)) {
            throw new BadRequestException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }
        
        // Optional: Kiểm tra ngày bắt đầu không quá xa trong quá khứ
        // if (ngayBatDau.isBefore(LocalDate.now().minusYears(1))) {
        //     throw new BadRequestException("Ngày bắt đầu không được quá 1 năm trong quá khứ");
        // }
    }
}
