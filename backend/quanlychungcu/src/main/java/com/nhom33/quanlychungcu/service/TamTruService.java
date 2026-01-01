package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.DangKyTamTruDTO;
import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.entity.TamTru;
import com.nhom33.quanlychungcu.exception.BadRequestException;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import com.nhom33.quanlychungcu.repository.TamTruRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Service xử lý nghiệp vụ Đăng ký Tạm trú.
 * 
 * Quy tắc nghiệp vụ:
 * - Người tạm trú phải được gắn với một hộ gia đình đang hoạt động.
 * - Hộ gia đình phải tồn tại và có trạng thái phù hợp.
 * - Số CCCD không được trùng với nhân khẩu thường trú đang ở (tránh nhầm lẫn).
 */
@Service
public class TamTruService {

    // Các trạng thái hộ gia đình được phép đăng ký tạm trú
    private static final Set<String> ALLOWED_HOUSEHOLD_STATUSES = Set.of(
        "Đang sử dụng", "Dang su dung", "Hoạt động", "Hoat dong", "Có người ở", "Đang ở"
    );

    private final TamTruRepository repo;
    private final HoGiaDinhRepository hoGiaDinhRepo;

    public TamTruService(TamTruRepository repo, HoGiaDinhRepository hoGiaDinhRepo) {
        this.repo = repo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
    }

    /**
     * Đăng ký tạm trú sử dụng DTO (API mới - khuyến khích sử dụng).
     * 
     * @param dto DTO chứa thông tin đăng ký tạm trú
     * @return TamTru đã được lưu
     * @throws ResourceNotFoundException nếu không tìm thấy hộ gia đình
     * @throws BadRequestException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public TamTru dangKyTamTru(DangKyTamTruDTO dto) {
        // === Bước 1: Kiểm tra hộ gia đình tồn tại ===
        HoGiaDinh hoGiaDinh = hoGiaDinhRepo.findById(dto.getHoGiaDinhId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy hộ gia đình với ID: " + dto.getHoGiaDinhId()
            ));
        
        // === Bước 2: Kiểm tra trạng thái hộ gia đình ===
        String trangThaiHo = hoGiaDinh.getTrangThai();
        if (trangThaiHo != null && !trangThaiHo.isBlank()) {
            if ("Trống".equalsIgnoreCase(trangThaiHo.trim()) || 
                "Không sử dụng".equalsIgnoreCase(trangThaiHo.trim())) {
                throw new BadRequestException(
                    "Hộ gia đình '" + hoGiaDinh.getMaHoGiaDinh() + "' đang ở trạng thái '" + 
                    trangThaiHo + "', không thể đăng ký tạm trú."
                );
            }
        }
        
        // === Bước 3: Validate ngày tháng ===
        validateDates(dto.getNgayBatDau(), dto.getNgayKetThuc());
        
        // === Bước 4: Tạo entity TamTru từ DTO ===
        TamTru tamTru = new TamTru();
        tamTru.setHoGiaDinh(hoGiaDinh);
        tamTru.setHoTen(dto.getHoTen());
        tamTru.setSoCCCD(dto.getSoCCCD());
        tamTru.setNgaySinh(dto.getNgaySinh());
        tamTru.setGioiTinh(dto.getGioiTinh());
        tamTru.setSoDienThoai(dto.getSoDienThoai());
        tamTru.setDiaChiThuongTru(dto.getDiaChiThuongTru());
        tamTru.setNgayBatDau(dto.getNgayBatDau());
        tamTru.setNgayKetThuc(dto.getNgayKetThuc());
        tamTru.setLyDo(dto.getLyDo());
        tamTru.setNgayDangKy(LocalDateTime.now());
        
        return repo.save(tamTru);
    }

    /**
     * Đăng ký tạm trú cho một người vào hộ gia đình (API legacy).
     * Khuyến khích sử dụng dangKyTamTru(DangKyTamTruDTO) thay thế.
     * 
     * @param tamTru Entity chứa thông tin đăng ký (bao gồm hoGiaDinh với id)
     * @return TamTru đã được lưu
     * @throws ResourceNotFoundException nếu không tìm thấy hộ gia đình
     * @throws BadRequestException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public TamTru create(TamTru tamTru) {
        // === Bước 1: Validate input cơ bản ===
        if (tamTru.getHoGiaDinh() == null || tamTru.getHoGiaDinh().getId() == null) {
            throw new BadRequestException("Thông tin hộ gia đình không được để trống");
        }
        
        if (tamTru.getHoTen() == null || tamTru.getHoTen().isBlank()) {
            throw new BadRequestException("Họ tên người tạm trú không được để trống");
        }
        
        Integer hoGiaDinhId = tamTru.getHoGiaDinh().getId();
        
        // === Bước 2: Kiểm tra hộ gia đình tồn tại ===
        HoGiaDinh hoGiaDinh = hoGiaDinhRepo.findById(hoGiaDinhId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy hộ gia đình với ID: " + hoGiaDinhId
            ));
        
        // === Bước 3: Kiểm tra trạng thái hộ gia đình (Optional - có thể bỏ nếu không cần) ===
        String trangThaiHo = hoGiaDinh.getTrangThai();
        if (trangThaiHo != null && !trangThaiHo.isBlank()) {
            // Nếu hộ đang "Trống" hoặc "Không sử dụng" thì có thể chặn
            if ("Trống".equalsIgnoreCase(trangThaiHo.trim()) || 
                "Không sử dụng".equalsIgnoreCase(trangThaiHo.trim())) {
                throw new BadRequestException(
                    "Hộ gia đình '" + hoGiaDinh.getMaHoGiaDinh() + "' đang ở trạng thái '" + 
                    trangThaiHo + "', không thể đăng ký tạm trú."
                );
            }
        }
        
        // === Bước 4: Validate ngày tháng ===
        validateDates(tamTru.getNgayBatDau(), tamTru.getNgayKetThuc());
        
        // === Bước 5: Gán entity HoGiaDinh đầy đủ và lưu ===
        tamTru.setHoGiaDinh(hoGiaDinh);
        
        // Đảm bảo ngày đăng ký được set
        if (tamTru.getNgayDangKy() == null) {
            tamTru.setNgayDangKy(java.time.LocalDateTime.now());
        }
        
        return repo.save(tamTru);
    }

    /**
     * Cập nhật thông tin tạm trú.
     * KHÔNG cho phép đổi hộ gia đình.
     */
    @Transactional
    public TamTru update(@NonNull Integer id, TamTru updated) {
        TamTru exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tạm trú với ID: " + id));
        
        // Validate ngày tháng nếu có thay đổi
        LocalDate ngayBatDau = updated.getNgayBatDau() != null ? updated.getNgayBatDau() : exist.getNgayBatDau();
        LocalDate ngayKetThuc = updated.getNgayKetThuc() != null ? updated.getNgayKetThuc() : exist.getNgayKetThuc();
        validateDates(ngayBatDau, ngayKetThuc);
        
        // Cập nhật các trường được phép
        if (updated.getHoTen() != null) exist.setHoTen(updated.getHoTen());
        if (updated.getSoCCCD() != null) exist.setSoCCCD(updated.getSoCCCD());
        if (updated.getNgaySinh() != null) exist.setNgaySinh(updated.getNgaySinh());
        if (updated.getSoDienThoai() != null) exist.setSoDienThoai(updated.getSoDienThoai());
        exist.setNgayBatDau(ngayBatDau);
        exist.setNgayKetThuc(ngayKetThuc);
        if (updated.getLyDo() != null) exist.setLyDo(updated.getLyDo());
        
        return repo.save(exist);
    }

    /**
     * Xóa bản ghi tạm trú.
     */
    @Transactional
    public void delete(@NonNull Integer id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy bản ghi tạm trú với ID: " + id);
        }
        repo.deleteById(id);
    }

    /**
     * Lấy thông tin tạm trú theo ID.
     */
    public TamTru getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tạm trú với ID: " + id));
    }

    /**
     * Tìm kiếm theo họ tên (có phân trang).
     */
    public Page<TamTru> searchByName(String hoTen, @NonNull Pageable pageable) {
        if (hoTen == null || hoTen.isBlank()) {
            return repo.findAll(pageable);
        }
        return repo.findByHoTenContainingIgnoreCase(hoTen, pageable);
    }

    /**
     * Lấy tất cả bản ghi (có phân trang).
     */
    public Page<TamTru> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
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
    }
}
