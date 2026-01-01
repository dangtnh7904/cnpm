package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.PhanAnh;
import com.nhom33.quanlychungcu.entity.PhanHoi;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import com.nhom33.quanlychungcu.repository.PhanAnhRepository;
import com.nhom33.quanlychungcu.repository.PhanHoiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhanAnhService {

    private final PhanAnhRepository phanAnhRepo;
    private final PhanHoiRepository phanHoiRepo;
    private final HoGiaDinhRepository hoGiaDinhRepo;

    public PhanAnhService(PhanAnhRepository phanAnhRepo,
                         PhanHoiRepository phanHoiRepo,
                         HoGiaDinhRepository hoGiaDinhRepo) {
        this.phanAnhRepo = phanAnhRepo;
        this.phanHoiRepo = phanHoiRepo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
    }

    @Transactional
    public PhanAnh create(PhanAnh phanAnh) {
        // Validate hộ gia đình
        if (phanAnh.getHoGiaDinh() == null || phanAnh.getHoGiaDinh().getId() == null) {
            throw new IllegalArgumentException("Hộ gia đình không được để trống");
        }
        
        Integer idHoGiaDinh = phanAnh.getHoGiaDinh().getId();
        hoGiaDinhRepo.findById(idHoGiaDinh)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + idHoGiaDinh));
        
        // Đảm bảo hoGiaDinh được load đầy đủ
        phanAnh.setHoGiaDinh(hoGiaDinhRepo.findById(idHoGiaDinh).get());
        
        return phanAnhRepo.save(phanAnh);
    }

    @Transactional
    public PhanAnh updateTrangThai(@NonNull Integer id, String trangThai) {
        PhanAnh phanAnh = phanAnhRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phản ánh với ID: " + id));
        
        phanAnh.setTrangThai(trangThai);
        return phanAnhRepo.save(phanAnh);
    }

    @Transactional
    public PhanHoi addPhanHoi(@NonNull Integer idPhanAnh, String noiDung, String nguoiTraLoi) {
        PhanAnh phanAnh = phanAnhRepo.findById(idPhanAnh)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phản ánh với ID: " + idPhanAnh));
        
        PhanHoi phanHoi = new PhanHoi();
        phanHoi.setPhanAnh(phanAnh);
        phanHoi.setNoiDung(noiDung);
        phanHoi.setNguoiTraLoi(nguoiTraLoi);
        
        phanHoi = phanHoiRepo.save(phanHoi);
        
        // Cập nhật trạng thái phản ánh
        phanAnh.setTrangThai("Đang xử lý");
        phanAnhRepo.save(phanAnh);
        
        return phanHoi;
    }

    public PhanAnh getById(@NonNull Integer id) {
        return phanAnhRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phản ánh với ID: " + id));
    }

    public Page<PhanAnh> findAll(@NonNull Pageable pageable) {
        return phanAnhRepo.findAll(pageable);
    }

    public Page<PhanAnh> findByHoGiaDinh(@NonNull Integer idHoGiaDinh, @NonNull Pageable pageable) {
        return phanAnhRepo.findByHoGiaDinhId(idHoGiaDinh, pageable);
    }

    public Page<PhanAnh> findByTrangThai(String trangThai, @NonNull Pageable pageable) {
        return phanAnhRepo.findByTrangThai(trangThai, pageable);
    }

    public Page<PhanAnh> search(Integer idHoGiaDinh, String trangThai, String tieuDe, @NonNull Pageable pageable) {
        return phanAnhRepo.search(idHoGiaDinh, trangThai, tieuDe, pageable);
    }

    public List<PhanHoi> getPhanHoiByPhanAnh(@NonNull Integer idPhanAnh) {
        return phanHoiRepo.findByPhanAnhId(idPhanAnh);
    }
}

