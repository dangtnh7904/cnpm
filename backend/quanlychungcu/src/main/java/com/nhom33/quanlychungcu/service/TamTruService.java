package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.TamTru;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.TamTruRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TamTruService {

    private final TamTruRepository repo;

    public TamTruService(TamTruRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public TamTru create(TamTru t) {
        // set NgayDangKy nếu null
        if (t.getNgayDangKy() == null) t.setNgayDangKy(java.time.LocalDateTime.now());
        return repo.save(t);
    }

    @Transactional
    public TamTru update(@NonNull Integer id, TamTru updated) {
        TamTru exist = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("TamTru not found: " + id));
        // set updated fields (ví dụ)
        exist.setHoTen(updated.getHoTen());
        exist.setSoCCCD(updated.getSoCCCD());
        exist.setNgaySinh(updated.getNgaySinh());
        exist.setSoDienThoai(updated.getSoDienThoai());
        exist.setNgayBatDau(updated.getNgayBatDau());
        exist.setNgayKetThuc(updated.getNgayKetThuc());
        exist.setLyDo(updated.getLyDo());
        return repo.save(exist);
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("TamTru not found: " + id);
        repo.deleteById(id);
    }

    public TamTru getById(@NonNull Integer id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("TamTru not found: " + id));
    }

    public Page<TamTru> searchByName(String hoTen, @NonNull Pageable pageable) {
        if (hoTen == null || hoTen.isBlank()) return repo.findAll(pageable);
        return repo.findByHoTenContainingIgnoreCase(hoTen, pageable);
    }

    public Page<TamTru> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
    }
}
