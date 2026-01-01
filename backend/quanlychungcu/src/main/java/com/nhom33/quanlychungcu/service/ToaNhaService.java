package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ToaNhaService {

    private final ToaNhaRepository repo;

    public ToaNhaService(ToaNhaRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ToaNha create(ToaNha toaNha) {
        // Kiểm tra tên tòa nhà đã tồn tại chưa
        if (repo.existsByTenToaNha(toaNha.getTenToaNha())) {
            throw new IllegalArgumentException(
                "Tên tòa nhà '" + toaNha.getTenToaNha() + "' đã tồn tại"
            );
        }
        return repo.save(toaNha);
    }

    @Transactional
    public ToaNha update(@NonNull Integer id, ToaNha updated) {
        ToaNha exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + id));

        // Kiểm tra nếu đổi tên và tên mới đã tồn tại
        if (!exist.getTenToaNha().equals(updated.getTenToaNha()) 
            && repo.existsByTenToaNha(updated.getTenToaNha())) {
            throw new IllegalArgumentException(
                "Tên tòa nhà '" + updated.getTenToaNha() + "' đã tồn tại"
            );
        }

        // Cập nhật thông tin
        exist.setTenToaNha(updated.getTenToaNha());
        exist.setMoTa(updated.getMoTa());

        return repo.save(exist);
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + id);
        }
        repo.deleteById(id);
    }

    public ToaNha getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + id));
    }

    public List<ToaNha> getAll() {
        return repo.findAll();
    }

    public Page<ToaNha> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<ToaNha> searchByTenToaNha(String tenToaNha, @NonNull Pageable pageable) {
        if (tenToaNha == null || tenToaNha.isBlank()) {
            return repo.findAll(pageable);
        }
        return repo.findByTenToaNhaContainingIgnoreCase(tenToaNha, pageable);
    }
}
