package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.dto.NhanKhauRequestDTO;
import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.entity.NhanKhau;
import com.nhom33.quanlychungcu.exception.BadRequestException;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import com.nhom33.quanlychungcu.repository.NhanKhauRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class NhanKhauService {

    private static final Logger log = LoggerFactory.getLogger(NhanKhauService.class);

    /**
     * Giá trị đặc biệt cho QuanHeVoiChuHo khi là chủ hộ.
     */
    private static final String CHU_HO_RELATION = "Chủ hộ";

    private final NhanKhauRepository repo;
    private final HoGiaDinhRepository hoGiaDinhRepo;

    public NhanKhauService(NhanKhauRepository repo, HoGiaDinhRepository hoGiaDinhRepo) {
        this.repo = repo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
    }

    /**
     * Kiểm tra xem quan hệ có phải là chủ hộ không (linh hoạt với các cách viết khác nhau).
     */
    private boolean isChuHoRelation(String quanHe) {
        if (quanHe == null) return false;
        String normalized = quanHe.toLowerCase().trim();
        return normalized.equals("chủ hộ") || 
               normalized.equals("chu ho") || 
               normalized.equals("chủ_hộ") ||
               normalized.equals("chu_ho") ||
               normalized.equals("chuho");
    }

    /**
     * Thêm nhân khẩu vào hộ gia đình (sử dụng DTO).
     * 
     * LUỒNG NGHIỆP VỤ:
     * 1. Kiểm tra CCCD đã tồn tại chưa.
     * 2. Kiểm tra hộ gia đình có tồn tại không.
     * 3. Nếu QuanHeVoiChuHo = "Chủ hộ":
     *    - Kiểm tra hộ đã có chủ hộ chưa.
     *    - Nếu có -> Ném BadRequestException.
     *    - Nếu chưa -> Lưu và tự động cập nhật TenChuHo trong bảng HoGiaDinh.
     * 4. Cập nhật trạng thái hộ gia đình thành "Đang ở" nếu đang "Trống".
     * 
     * @param dto Thông tin nhân khẩu
     * @return NhanKhau đã lưu
     */
    @Transactional
    public NhanKhau addNhanKhauWithValidation(NhanKhauRequestDTO dto) {
        log.info("Thêm nhân khẩu {} vào hộ gia đình ID: {}", dto.getHoTen(), dto.getHoGiaDinhId());

        // === Bước 1: Kiểm tra CCCD đã tồn tại chưa ===
        if (repo.existsBySoCCCD(dto.getSoCCCD())) {
            throw new BadRequestException(
                "Số CCCD '" + dto.getSoCCCD() + "' đã tồn tại trong hệ thống"
            );
        }

        // === Bước 2: Kiểm tra hộ gia đình tồn tại ===
        HoGiaDinh hoGiaDinh = hoGiaDinhRepo.findById(dto.getHoGiaDinhId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy hộ gia đình với ID: " + dto.getHoGiaDinhId()
            ));

        // === Bước 3: Kiểm tra và xử lý nếu là Chủ hộ ===
        boolean isChuHo = isChuHoRelation(dto.getQuanHeVoiChuHo());
        
        if (isChuHo) {
            // Kiểm tra hộ đã có chủ hộ chưa -> nếu có thì chuyển thành "Người thân"
            Optional<NhanKhau> existingChuHo = repo.findChuHoByHoGiaDinhId(dto.getHoGiaDinhId());
            if (existingChuHo.isPresent()) {
                NhanKhau oldChuHo = existingChuHo.get();
                oldChuHo.setQuanHeVoiChuHo("Người thân");
                oldChuHo.setLaChuHo(false);
                repo.save(oldChuHo);
                log.info("Đã chuyển chủ hộ cũ '{}' thành Người thân", oldChuHo.getHoTen());
            }
        }

        // === Bước 4: Tạo entity NhanKhau ===
        NhanKhau nhanKhau = new NhanKhau();
        nhanKhau.setHoTen(dto.getHoTen());
        nhanKhau.setSoCCCD(dto.getSoCCCD());
        nhanKhau.setNgaySinh(dto.getNgaySinh());
        nhanKhau.setGioiTinh(dto.getGioiTinh());
        nhanKhau.setSoDienThoai(dto.getSoDienThoai());
        nhanKhau.setEmail(dto.getEmail());
        nhanKhau.setQuanHeVoiChuHo(dto.getQuanHeVoiChuHo());
        nhanKhau.setLaChuHo(isChuHo);
        nhanKhau.setNgayChuyenDen(dto.getNgayChuyenDen() != null ? dto.getNgayChuyenDen() : LocalDate.now());
        nhanKhau.setTrangThai("Đang ở");
        nhanKhau.setHoGiaDinh(hoGiaDinh);

        // Lưu nhân khẩu
        NhanKhau savedNhanKhau = repo.save(nhanKhau);

        // === Bước 5: Cập nhật thông tin hộ gia đình ===
        if (isChuHo) {
            // Cập nhật TenChuHo trong bảng HoGiaDinh
            hoGiaDinh.setTenChuHo(dto.getHoTen());
            
            // Cập nhật thông tin liên hệ nếu có
            if (dto.getSoDienThoai() != null && !dto.getSoDienThoai().isBlank()) {
                hoGiaDinh.setSoDienThoaiLienHe(dto.getSoDienThoai());
            }
            if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
                hoGiaDinh.setEmailLienHe(dto.getEmail());
            }
            
            log.info("Đã cập nhật chủ hộ của hộ {} thành {}", hoGiaDinh.getMaHoGiaDinh(), dto.getHoTen());
        }

        // Cập nhật trạng thái hộ gia đình thành "Đang ở" nếu đang "Trống"
        String trangThaiHo = hoGiaDinh.getTrangThai();
        if (trangThaiHo == null || trangThaiHo.isBlank() || "Trống".equalsIgnoreCase(trangThaiHo)) {
            hoGiaDinh.setTrangThai("Đang ở");
        }

        // Lưu hộ gia đình
        hoGiaDinhRepo.save(hoGiaDinh);

        log.info("Thêm thành công nhân khẩu {} (ID: {}) vào hộ {}", 
                 savedNhanKhau.getHoTen(), savedNhanKhau.getId(), hoGiaDinh.getMaHoGiaDinh());

        return savedNhanKhau;
    }

    /**
     * Cập nhật thông tin nhân khẩu (sử dụng DTO).
     * 
     * QUY TẮC NGHIÊM NGẶT:
     * - KHÔNG cho phép thay đổi trường TrangThai qua API này.
     * - Việc thay đổi trạng thái phải thực hiện qua API nghiệp vụ riêng (Tạm vắng/Tạm trú).
     * - Nếu đổi QuanHeVoiChuHo thành "Chủ hộ", phải kiểm tra hộ đã có chủ hộ chưa.
     * 
     * @param id ID nhân khẩu cần cập nhật
     * @param dto Thông tin cập nhật
     * @return NhanKhau đã cập nhật
     */
    @Transactional
    public NhanKhau updateNhanKhauWithValidation(@NonNull Integer id, NhanKhauRequestDTO dto) {
        log.info("Cập nhật nhân khẩu ID: {}", id);

        // === Bước 1: Tìm nhân khẩu hiện tại ===
        NhanKhau exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân khẩu với ID: " + id));

        // === Bước 2: Kiểm tra nếu đổi CCCD và CCCD mới đã tồn tại ===
        if (!dto.getSoCCCD().equals(exist.getSoCCCD()) && repo.existsBySoCCCD(dto.getSoCCCD())) {
            throw new BadRequestException(
                "Số CCCD '" + dto.getSoCCCD() + "' đã tồn tại trong hệ thống"
            );
        }

        // === Bước 3: Xử lý thay đổi QuanHeVoiChuHo ===
        boolean wasChiHo = Boolean.TRUE.equals(exist.getLaChuHo());
        boolean willBeChuHo = isChuHoRelation(dto.getQuanHeVoiChuHo());
        
        HoGiaDinh hoGiaDinh = exist.getHoGiaDinh();
        
        if (willBeChuHo && !wasChiHo) {
            // Đang muốn trở thành chủ hộ - chuyển chủ hộ cũ thành Người thân
            Optional<NhanKhau> existingChuHo = repo.findChuHoByHoGiaDinhId(hoGiaDinh.getId());
            if (existingChuHo.isPresent() && !existingChuHo.get().getId().equals(id)) {
                NhanKhau oldChuHo = existingChuHo.get();
                oldChuHo.setQuanHeVoiChuHo("Người thân");
                oldChuHo.setLaChuHo(false);
                repo.save(oldChuHo);
                log.info("Đã chuyển chủ hộ cũ '{}' thành Người thân", oldChuHo.getHoTen());
            }
        }

        // === Bước 4: Cập nhật các trường ĐƯỢC PHÉP thay đổi ===
        exist.setHoTen(dto.getHoTen());
        exist.setSoCCCD(dto.getSoCCCD());
        exist.setNgaySinh(dto.getNgaySinh());
        exist.setGioiTinh(dto.getGioiTinh());
        exist.setSoDienThoai(dto.getSoDienThoai());
        exist.setEmail(dto.getEmail());
        exist.setQuanHeVoiChuHo(dto.getQuanHeVoiChuHo());
        exist.setLaChuHo(willBeChuHo);
        if (dto.getNgayChuyenDen() != null) {
            exist.setNgayChuyenDen(dto.getNgayChuyenDen());
        }

        // QUAN TRỌNG: KHÔNG cập nhật TrangThai từ DTO
        // TrangThai được quản lý bởi các API nghiệp vụ riêng (Tạm vắng, Tạm trú, Chuyển đi...)

        // Lưu nhân khẩu
        NhanKhau savedNhanKhau = repo.save(exist);

        // === Bước 5: Cập nhật TenChuHo nếu cần ===
        if (willBeChuHo) {
            hoGiaDinh.setTenChuHo(dto.getHoTen());
            if (dto.getSoDienThoai() != null && !dto.getSoDienThoai().isBlank()) {
                hoGiaDinh.setSoDienThoaiLienHe(dto.getSoDienThoai());
            }
            if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
                hoGiaDinh.setEmailLienHe(dto.getEmail());
            }
            hoGiaDinhRepo.save(hoGiaDinh);
            log.info("Đã cập nhật chủ hộ của hộ {} thành {}", hoGiaDinh.getMaHoGiaDinh(), dto.getHoTen());
        } else if (wasChiHo && !willBeChuHo) {
            // Người này không còn là chủ hộ nữa
            hoGiaDinh.setTenChuHo(HoGiaDinhService.DEFAULT_CHU_HO_NAME);
            hoGiaDinhRepo.save(hoGiaDinh);
            log.warn("Nhân khẩu {} không còn là chủ hộ, hộ {} chưa có chủ hộ mới", 
                     exist.getHoTen(), hoGiaDinh.getMaHoGiaDinh());
        }

        log.info("Cập nhật thành công nhân khẩu ID: {}", id);
        return savedNhanKhau;
    }

    // ===== CÁC METHOD LEGACY - GIỮ LẠI ĐỂ TƯƠNG THÍCH =====

    @Transactional
    public NhanKhau create(NhanKhau nhanKhau) {
        // Kiểm tra số CCCD đã tồn tại chưa
        if (repo.existsBySoCCCD(nhanKhau.getSoCCCD())) {
            throw new IllegalArgumentException(
                "Số CCCD '" + nhanKhau.getSoCCCD() + "' đã tồn tại"
            );
        }

        // Kiểm tra hộ gia đình có tồn tại không
        HoGiaDinh hoGiaDinhInput = nhanKhau.getHoGiaDinh();
        if (hoGiaDinhInput != null) {
            Integer hoGiaDinhId = hoGiaDinhInput.getId();
            if (hoGiaDinhId != null) {
                HoGiaDinh hoGiaDinh = hoGiaDinhRepo.findById(hoGiaDinhId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hộ gia đình với ID: " + hoGiaDinhId
                    ));
                nhanKhau.setHoGiaDinh(hoGiaDinh);
            }
        }

        // Nếu là chủ hộ, kiểm tra đã có chủ hộ chưa
        HoGiaDinh hoGiaDinhCheck = nhanKhau.getHoGiaDinh();
        if (Boolean.TRUE.equals(nhanKhau.getLaChuHo()) && hoGiaDinhCheck != null) {
            Integer checkId = hoGiaDinhCheck.getId();
            if (checkId != null) {
                repo.findChuHoByHoGiaDinhId(checkId)
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                            "Hộ gia đình đã có chủ hộ: " + existing.getHoTen()
                        );
                    });
            }
        }

        return repo.save(nhanKhau);
    }

    /**
     * Thêm nhân khẩu vào hộ gia đình với logic "Tự động làm chủ hộ"
     * - Nếu hộ gia đình chưa có ai hoặc trạng thái trống, người đầu tiên sẽ là Chủ hộ
     * - Cập nhật trạng thái hộ gia đình thành "Đang ở" nếu trước đó trống
     */
    @Transactional
    public NhanKhau addNhanKhau(NhanKhau nhanKhau, Integer hoGiaDinhId) {
        // Kiểm tra số CCCD đã tồn tại chưa
        if (repo.existsBySoCCCD(nhanKhau.getSoCCCD())) {
            throw new IllegalArgumentException(
                "Số CCCD '" + nhanKhau.getSoCCCD() + "' đã tồn tại"
            );
        }

        // Tìm hộ gia đình
        HoGiaDinh hoGiaDinh = hoGiaDinhRepo.findById(hoGiaDinhId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy hộ gia đình với ID: " + hoGiaDinhId
            ));

        // Gán hộ gia đình cho nhân khẩu
        nhanKhau.setHoGiaDinh(hoGiaDinh);

        // Logic "Tự động làm Chủ hộ"
        List<NhanKhau> existingResidents = repo.findByHoGiaDinhId(hoGiaDinhId);
        String currentStatus = hoGiaDinh.getTrangThai();
        
        boolean isHouseholdEmpty = existingResidents.isEmpty();
        boolean isStatusEmpty = currentStatus == null || currentStatus.isBlank();

        if (isHouseholdEmpty || isStatusEmpty) {
            // Đây là người đầu tiên trong hộ -> tự động làm Chủ hộ
            nhanKhau.setQuanHeVoiChuHo("Chủ hộ");
            nhanKhau.setLaChuHo(true);
            
            // Cập nhật tên chủ hộ trong hộ gia đình
            hoGiaDinh.setTenChuHo(nhanKhau.getHoTen());
            
            // Cập nhật thông tin liên hệ nếu có
            if (nhanKhau.getSoDienThoai() != null && !nhanKhau.getSoDienThoai().isBlank()) {
                hoGiaDinh.setSoDienThoaiLienHe(nhanKhau.getSoDienThoai());
            }
            if (nhanKhau.getEmail() != null && !nhanKhau.getEmail().isBlank()) {
                hoGiaDinh.setEmailLienHe(nhanKhau.getEmail());
            }
        }

        // Cập nhật trạng thái hộ gia đình thành "Đang ở" nếu trước đó trống
        if (isStatusEmpty) {
            hoGiaDinh.setTrangThai("Đang ở");
        }

        // Lưu hộ gia đình (nếu có thay đổi)
        hoGiaDinhRepo.save(hoGiaDinh);

        // Lưu nhân khẩu
        return repo.save(nhanKhau);
    }

    @Transactional
    public NhanKhau update(@NonNull Integer id, NhanKhau updated) {
        NhanKhau exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân khẩu với ID: " + id));

        // Kiểm tra nếu đổi CCCD và CCCD mới đã tồn tại
        if (!updated.getSoCCCD().equals(exist.getSoCCCD()) 
            && repo.existsBySoCCCD(updated.getSoCCCD())) {
            throw new BadRequestException(
                "Số CCCD '" + updated.getSoCCCD() + "' đã tồn tại"
            );
        }

        // Nếu đổi thành chủ hộ, kiểm tra đã có chủ hộ chưa
        boolean wasChiHo = Boolean.TRUE.equals(exist.getLaChuHo());
        boolean willBeChuHo = isChuHoRelation(updated.getQuanHeVoiChuHo());
        
        if (willBeChuHo && !wasChiHo) {
            HoGiaDinh hoGiaDinh = exist.getHoGiaDinh();
            if (hoGiaDinh != null) {
                repo.findChuHoByHoGiaDinhId(hoGiaDinh.getId())
                    .ifPresent(chuHo -> {
                        if (!chuHo.getId().equals(id)) {
                            throw new BadRequestException(
                                "Hộ gia đình này đã có chủ hộ: " + chuHo.getHoTen() + 
                                ". Vui lòng chọn quan hệ khác."
                            );
                        }
                    });
            }
        }

        // Cập nhật thông tin - KHÔNG bao gồm TrangThai
        exist.setHoTen(updated.getHoTen());
        exist.setNgaySinh(updated.getNgaySinh());
        exist.setGioiTinh(updated.getGioiTinh());
        exist.setSoCCCD(updated.getSoCCCD());
        exist.setSoDienThoai(updated.getSoDienThoai());
        exist.setEmail(updated.getEmail());
        exist.setQuanHeVoiChuHo(updated.getQuanHeVoiChuHo());
        exist.setLaChuHo(willBeChuHo);
        exist.setNgayChuyenDen(updated.getNgayChuyenDen());
        
        // QUAN TRỌNG: KHÔNG cho phép thay đổi TrangThai qua API update thông thường
        // Việc thay đổi trạng thái phải thực hiện qua API nghiệp vụ riêng (Tạm vắng/Tạm trú)
        // exist.setTrangThai(updated.getTrangThai()); // DISABLED

        NhanKhau savedNhanKhau = repo.save(exist);
        
        // Cập nhật TenChuHo trong HoGiaDinh nếu cần
        HoGiaDinh hoGiaDinh = exist.getHoGiaDinh();
        if (hoGiaDinh != null) {
            if (willBeChuHo) {
                hoGiaDinh.setTenChuHo(updated.getHoTen());
                hoGiaDinhRepo.save(hoGiaDinh);
            } else if (wasChiHo && !willBeChuHo) {
                hoGiaDinh.setTenChuHo(HoGiaDinhService.DEFAULT_CHU_HO_NAME);
                hoGiaDinhRepo.save(hoGiaDinh);
            }
        }

        return savedNhanKhau;
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        NhanKhau nhanKhau = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân khẩu với ID: " + id));

        // Không cho xóa chủ hộ
        if (Boolean.TRUE.equals(nhanKhau.getLaChuHo())) {
            throw new IllegalArgumentException(
                "Không thể xóa chủ hộ. Vui lòng chuyển quyền chủ hộ cho người khác trước."
            );
        }

        repo.deleteById(id);
    }

    public NhanKhau getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân khẩu với ID: " + id));
    }

    public NhanKhau getBySoCCCD(String soCCCD) {
        return repo.findBySoCCCD(soCCCD)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân khẩu với CCCD: " + soCCCD));
    }

    public Page<NhanKhau> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
    }

    public List<NhanKhau> findByHoGiaDinh(@NonNull Integer idHoGiaDinh) {
        return repo.findByHoGiaDinhId(idHoGiaDinh);
    }

    public Page<NhanKhau> searchByHoTen(String hoTen, @NonNull Pageable pageable) {
        if (hoTen == null || hoTen.isBlank()) {
            return repo.findAll(pageable);
        }
        return repo.findByHoTenContainingIgnoreCase(hoTen, pageable);
    }

    public Page<NhanKhau> search(String hoTen, String soCCCD, String gioiTinh, 
                                  String trangThai, Integer idHoGiaDinh, @NonNull Pageable pageable) {
        return repo.search(hoTen, soCCCD, gioiTinh, trangThai, idHoGiaDinh, pageable);
    }

    public long countByHoGiaDinh(@NonNull Integer idHoGiaDinh) {
        return repo.countByHoGiaDinhId(idHoGiaDinh);
    }

    public long countByGioiTinh(String gioiTinh) {
        return repo.countByGioiTinh(gioiTinh);
    }
}
