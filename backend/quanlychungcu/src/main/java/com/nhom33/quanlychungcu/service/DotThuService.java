package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.DotThu;
import com.nhom33.quanlychungcu.entity.DotThuLoaiPhi;
import com.nhom33.quanlychungcu.entity.LoaiPhi;
import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.DotThuLoaiPhiRepository;
import com.nhom33.quanlychungcu.repository.DotThuRepository;
import com.nhom33.quanlychungcu.repository.LoaiPhiRepository;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service: Quản lý Đợt Thu.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Mỗi đợt thu thuộc về một tòa nhà cụ thể
 * - Cho phép nhiều tòa nhà có cùng tên đợt thu (VD: Tòa A và Tòa B đều có "Tháng 10")
 * - Khi thêm loại phí Điện/Nước (biến đổi), Frontend cần hiển thị Tab Ghi Chỉ Số
 */
@Service
public class DotThuService {

    private final DotThuRepository repo;
    private final DotThuLoaiPhiRepository dotThuLoaiPhiRepo;
    private final LoaiPhiRepository loaiPhiRepo;
    private final ToaNhaRepository toaNhaRepo;
    
    // Danh sách tên loại phí biến đổi (cần ghi chỉ số)
    private static final List<String> UTILITY_FEES = Arrays.asList("Điện", "Nước");
    
    // Danh sách tên loại phí bắt buộc (không được xóa)
    private static final List<String> MANDATORY_FEES = Arrays.asList("Điện", "Nước");

    public DotThuService(DotThuRepository repo, 
                         DotThuLoaiPhiRepository dotThuLoaiPhiRepo,
                         LoaiPhiRepository loaiPhiRepo,
                         ToaNhaRepository toaNhaRepo) {
        this.repo = repo;
        this.dotThuLoaiPhiRepo = dotThuLoaiPhiRepo;
        this.loaiPhiRepo = loaiPhiRepo;
        this.toaNhaRepo = toaNhaRepo;
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
        
        // Tự động thêm các loại phí bắt buộc (Điện, Nước)
        addMandatoryFees(saved);
        
        return saved;
    }
    
    /**
     * Thêm các loại phí bắt buộc vào đợt thu.
     */
    private void addMandatoryFees(DotThu dotThu) {
        for (String tenLoaiPhi : MANDATORY_FEES) {
            loaiPhiRepo.findByTenLoaiPhi(tenLoaiPhi).ifPresent(loaiPhi -> {
                if (!dotThuLoaiPhiRepo.existsByDotThuIdAndLoaiPhiId(dotThu.getId(), loaiPhi.getId())) {
                    DotThuLoaiPhi config = new DotThuLoaiPhi(dotThu, loaiPhi);
                    dotThuLoaiPhiRepo.save(config);
                }
            });
        }
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
     * Lấy danh sách loại phí trong đợt thu.
     */
    public List<DotThuLoaiPhi> getFeesInPeriod(Integer dotThuId) {
        if (!repo.existsById(dotThuId)) {
            throw new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + dotThuId);
        }
        return dotThuLoaiPhiRepo.findByDotThuId(dotThuId);
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
        
        // Kiểm tra phí bắt buộc
        if (MANDATORY_FEES.contains(loaiPhi.getTenLoaiPhi())) {
            throw new IllegalArgumentException("Không thể xóa loại phí bắt buộc: " + loaiPhi.getTenLoaiPhi());
        }
        
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
     * Kiểm tra loại phí có phải bắt buộc không.
     */
    public boolean isMandatoryFee(Integer loaiPhiId) {
        return loaiPhiRepo.findById(loaiPhiId)
            .map(lp -> MANDATORY_FEES.contains(lp.getTenLoaiPhi()))
            .orElse(false);
    }
    
    /**
     * Kiểm tra loại phí có phải phí biến đổi (cần ghi chỉ số) không.
     */
    public boolean isUtilityFee(Integer loaiPhiId) {
        return loaiPhiRepo.findById(loaiPhiId)
            .map(lp -> UTILITY_FEES.contains(lp.getTenLoaiPhi()))
            .orElse(false);
    }
}

