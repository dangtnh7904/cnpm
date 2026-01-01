package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.TamVang;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.TamVangRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TamVangService {

    private final TamVangRepository repo;

    public TamVangService(TamVangRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public TamVang create(TamVang t) {
        if (t.getNgayDangKy() == null) t.setNgayDangKy(java.time.LocalDateTime.now());
        return repo.save(t);
    }

    @Transactional
    public TamVang update(@NonNull Integer id, TamVang updated) {
        TamVang exist = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("TamVang not found: " + id));
        exist.setNgayBatDau(updated.getNgayBatDau());
        exist.setNgayKetThuc(updated.getNgayKetThuc());
        exist.setNoiDen(updated.getNoiDen());
        exist.setLyDo(updated.getLyDo());
        return repo.save(exist);
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("TamVang not found: " + id);
        repo.deleteById(id);
    }

    public TamVang getById(@NonNull Integer id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("TamVang not found: " + id));
    }

    public Page<TamVang> searchByNoiDen(String noiDen, @NonNull Pageable pageable) {
        if (noiDen == null || noiDen.isBlank()) return repo.findAll(pageable);
        return repo.findByNoiDenContainingIgnoreCase(noiDen, pageable);
    }
}
