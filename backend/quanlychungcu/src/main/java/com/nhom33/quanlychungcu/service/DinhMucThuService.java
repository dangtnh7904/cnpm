package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.DinhMucThu;
import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.entity.LoaiPhi;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.DinhMucThuRepository;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import com.nhom33.quanlychungcu.repository.LoaiPhiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DinhMucThuService {

    private final DinhMucThuRepository repo;
    private final HoGiaDinhRepository hoGiaDinhRepo;
    private final LoaiPhiRepository loaiPhiRepo;

    public DinhMucThuService(DinhMucThuRepository repo,
                            HoGiaDinhRepository hoGiaDinhRepo,
                            LoaiPhiRepository loaiPhiRepo) {
        this.repo = repo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
        this.loaiPhiRepo = loaiPhiRepo;
    }

    @Transactional
    public DinhMucThu create(DinhMucThu dinhMuc) {
        // Validate hộ gia đình
        HoGiaDinh hoGiaDinh = hoGiaDinhRepo.findById(dinhMuc.getHoGiaDinh().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình"));
        
        // Validate loại phí
        LoaiPhi loaiPhi = loaiPhiRepo.findById(dinhMuc.getLoaiPhi().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí"));
        
        // Kiểm tra đã có định mức chưa
        Optional<DinhMucThu> existing = repo.findByHoGiaDinhIdAndLoaiPhiId(
            hoGiaDinh.getId(), loaiPhi.getId());
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Hộ gia đình đã có định mức cho loại phí này");
        }
        
        dinhMuc.setHoGiaDinh(hoGiaDinh);
        dinhMuc.setLoaiPhi(loaiPhi);
        
        return repo.save(dinhMuc);
    }

    @Transactional
    public DinhMucThu update(@NonNull Integer id, DinhMucThu updated) {
        DinhMucThu exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy định mức thu với ID: " + id));
        
        exist.setSoLuong(updated.getSoLuong());
        exist.setGhiChu(updated.getGhiChu());
        
        return repo.save(exist);
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy định mức thu với ID: " + id);
        }
        repo.deleteById(id);
    }

    public DinhMucThu getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy định mức thu với ID: " + id));
    }

    public List<DinhMucThu> findByHoGiaDinh(@NonNull Integer idHoGiaDinh) {
        return repo.findByHoGiaDinhId(idHoGiaDinh);
    }

    public Page<DinhMucThu> findByHoGiaDinh(@NonNull Integer idHoGiaDinh, @NonNull Pageable pageable) {
        return repo.findByHoGiaDinhId(idHoGiaDinh, pageable);
    }
}

