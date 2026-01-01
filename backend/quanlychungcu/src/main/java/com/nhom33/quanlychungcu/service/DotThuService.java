package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.DotThu;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.DotThuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DotThuService {

    private final DotThuRepository repo;

    public DotThuService(DotThuRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public DotThu create(DotThu dotThu) {
        // Validate ngày
        if (dotThu.getNgayKetThuc().isBefore(dotThu.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        return repo.save(dotThu);
    }

    @Transactional
    public DotThu update(@NonNull Integer id, DotThu updated) {
        DotThu exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt thu với ID: " + id));
        
        if (updated.getNgayKetThuc().isBefore(updated.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        exist.setTenDotThu(updated.getTenDotThu());
        exist.setLoaiDotThu(updated.getLoaiDotThu());
        exist.setNgayBatDau(updated.getNgayBatDau());
        exist.setNgayKetThuc(updated.getNgayKetThuc());
        
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

    public Page<DotThu> search(String tenDotThu, String loaiDotThu, LocalDate ngayBatDau, LocalDate ngayKetThuc, @NonNull Pageable pageable) {
        return repo.search(tenDotThu, loaiDotThu, ngayBatDau, ngayKetThuc, pageable);
    }
}

