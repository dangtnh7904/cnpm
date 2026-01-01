package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.LoaiPhi;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.LoaiPhiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoaiPhiService {

    private final LoaiPhiRepository repo;

    public LoaiPhiService(LoaiPhiRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public LoaiPhi create(LoaiPhi loaiPhi) {
        return repo.save(loaiPhi);
    }

    @Transactional
    public LoaiPhi update(@NonNull Integer id, LoaiPhi updated) {
        LoaiPhi exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
        
        exist.setTenLoaiPhi(updated.getTenLoaiPhi());
        exist.setDonGia(updated.getDonGia());
        exist.setDonViTinh(updated.getDonViTinh());
        exist.setLoaiThu(updated.getLoaiThu());
        exist.setMoTa(updated.getMoTa());
        exist.setDangHoatDong(updated.getDangHoatDong());
        
        return repo.save(exist);
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id);
        }
        repo.deleteById(id);
    }

    public LoaiPhi getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phí với ID: " + id));
    }

    public Page<LoaiPhi> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<LoaiPhi> findByDangHoatDong(Boolean dangHoatDong, @NonNull Pageable pageable) {
        return repo.findByDangHoatDong(dangHoatDong, pageable);
    }

    public Page<LoaiPhi> search(String tenLoaiPhi, String loaiThu, Boolean dangHoatDong, @NonNull Pageable pageable) {
        return repo.search(tenLoaiPhi, loaiThu, dangHoatDong, pageable);
    }

    public List<LoaiPhi> findAllActive() {
        return repo.findByDangHoatDongTrue();
    }
}

