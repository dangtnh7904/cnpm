package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.PhanAnh;
import com.nhom33.quanlychungcu.entity.PhanHoi;
import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.entity.UserAccount;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.PhanAnhRepository;
import com.nhom33.quanlychungcu.repository.PhanHoiRepository;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;
import com.nhom33.quanlychungcu.repository.UserToaNhaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service quản lý Phản ánh.
 * 
 * LOGIC NGHIỆP VỤ:
 * - RESIDENT: Gửi phản ánh cho tòa nhà mình thuộc (qua UserToaNha)
 * - MANAGER: Xem và phản hồi phản ánh của tòa nhà mình quản lý
 * - ADMIN: Xem tất cả
 */
@Service
public class PhanAnhService {

    private final PhanAnhRepository phanAnhRepo;
    private final PhanHoiRepository phanHoiRepo;
    private final ToaNhaRepository toaNhaRepo;
    private final UserToaNhaRepository userToaNhaRepo;
    private final SecurityHelper securityHelper;

    public PhanAnhService(PhanAnhRepository phanAnhRepo,
                         PhanHoiRepository phanHoiRepo,
                         ToaNhaRepository toaNhaRepo,
                         UserToaNhaRepository userToaNhaRepo,
                         SecurityHelper securityHelper) {
        this.phanAnhRepo = phanAnhRepo;
        this.phanHoiRepo = phanHoiRepo;
        this.toaNhaRepo = toaNhaRepo;
        this.userToaNhaRepo = userToaNhaRepo;
        this.securityHelper = securityHelper;
    }

    /**
     * Tạo phản ánh mới.
     * User chỉ được gửi phản ánh cho tòa nhà mình thuộc.
     */
    @Transactional
    public PhanAnh create(Integer toaNhaId, String tieuDe, String noiDung) {
        UserAccount currentUser = securityHelper.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Bạn cần đăng nhập để gửi phản ánh");
        }
        
        // Kiểm tra user có thuộc tòa nhà này không
        if (!securityHelper.canAccessBuilding(toaNhaId)) {
            throw new AccessDeniedException("Bạn không thuộc tòa nhà này nên không thể gửi phản ánh");
        }
        
        ToaNha toaNha = toaNhaRepo.findById(toaNhaId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + toaNhaId));
        
        PhanAnh phanAnh = new PhanAnh(currentUser, toaNha, tieuDe, noiDung);
        return phanAnhRepo.save(phanAnh);
    }

    @Transactional
    public PhanAnh updateTrangThai(@NonNull Integer id, String trangThai) {
        PhanAnh phanAnh = phanAnhRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phản ánh với ID: " + id));
        
        // Kiểm tra quyền (chỉ Manager của tòa hoặc Admin)
        if (!securityHelper.canManageBuilding(phanAnh.getToaNha().getId())) {
            throw new AccessDeniedException("Bạn không có quyền cập nhật trạng thái phản ánh này");
        }
        
        phanAnh.setTrangThai(trangThai);
        return phanAnhRepo.save(phanAnh);
    }

    @Transactional
    public PhanHoi addPhanHoi(@NonNull Integer idPhanAnh, String noiDung, String nguoiTraLoi) {
        PhanAnh phanAnh = phanAnhRepo.findById(idPhanAnh)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phản ánh với ID: " + idPhanAnh));
        
        // Kiểm tra quyền (chỉ Manager của tòa hoặc Admin)
        if (!securityHelper.canManageBuilding(phanAnh.getToaNha().getId())) {
            throw new AccessDeniedException("Bạn không có quyền phản hồi phản ánh này");
        }
        
        PhanHoi phanHoi = new PhanHoi();
        phanHoi.setPhanAnh(phanAnh);
        phanHoi.setNoiDung(noiDung);
        phanHoi.setNguoiTraLoi(nguoiTraLoi);
        
        phanHoi = phanHoiRepo.save(phanHoi);
        
        // Cập nhật trạng thái phản ánh thành "Đã xử lý" khi có phản hồi
        phanAnh.setTrangThai("Đã xử lý");
        phanAnhRepo.save(phanAnh);
        
        return phanHoi;
    }

    public PhanAnh getById(@NonNull Integer id) {
        PhanAnh phanAnh = phanAnhRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phản ánh với ID: " + id));
        
        // Kiểm tra quyền xem
        if (!securityHelper.canAccessBuilding(phanAnh.getToaNha().getId())) {
            throw new AccessDeniedException("Bạn không có quyền xem phản ánh này");
        }
        
        return phanAnh;
    }

    /**
     * Lấy danh sách phản ánh.
     * - ADMIN: Xem tất cả
     * - MANAGER: Xem phản ánh của tòa nhà mình
     * - RESIDENT: Xem phản ánh của mình
     */
    public Page<PhanAnh> findAll(@NonNull Pageable pageable) {
        if (securityHelper.canViewAll()) {
            return phanAnhRepo.findAll(pageable);
        }
        
        // MANAGER xem phản ánh của tòa nhà mình
        if (securityHelper.isManager()) {
            List<Integer> toaNhaIds = securityHelper.getAccessibleBuildingIds();
            if (toaNhaIds.isEmpty()) {
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }
            return phanAnhRepo.findByToaNhaIdIn(toaNhaIds, pageable);
        }
        
        // RESIDENT xem phản ánh của mình
        UserAccount user = securityHelper.getCurrentUser();
        if (user != null) {
            return phanAnhRepo.findByUserId(user.getId(), pageable);
        }
        
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    /**
     * Lấy phản ánh theo tòa nhà (cho Manager).
     */
    public Page<PhanAnh> findByToaNha(@NonNull Integer toaNhaId, @NonNull Pageable pageable) {
        if (!securityHelper.canAccessBuilding(toaNhaId)) {
            throw new AccessDeniedException("Bạn không có quyền xem phản ánh của tòa nhà này");
        }
        return phanAnhRepo.findByToaNhaId(toaNhaId, pageable);
    }

    /**
     * Lấy phản ánh của user hiện tại (cho Resident).
     */
    public Page<PhanAnh> findMyPhanAnh(@NonNull Pageable pageable) {
        UserAccount user = securityHelper.getCurrentUser();
        if (user == null) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        return phanAnhRepo.findByUserId(user.getId(), pageable);
    }

    public Page<PhanAnh> findByTrangThai(String trangThai, @NonNull Pageable pageable) {
        return phanAnhRepo.findByTrangThai(trangThai, pageable);
    }

    public Page<PhanAnh> search(Integer toaNhaId, String trangThai, String tieuDe, @NonNull Pageable pageable) {
        // Multi-tenancy
        if (securityHelper.canViewAll()) {
            return phanAnhRepo.search(null, toaNhaId, trangThai, tieuDe, pageable);
        }
        
        List<Integer> toaNhaIds = securityHelper.getAccessibleBuildingIds();
        if (toaNhaIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        return phanAnhRepo.searchByToaNhaIds(toaNhaIds, trangThai, tieuDe, pageable);
    }

    public List<PhanHoi> getPhanHoiByPhanAnh(@NonNull Integer idPhanAnh) {
        return phanHoiRepo.findByPhanAnhId(idPhanAnh);
    }
}

