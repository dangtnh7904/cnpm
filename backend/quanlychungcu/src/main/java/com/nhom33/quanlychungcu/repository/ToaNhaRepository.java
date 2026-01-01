package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.ToaNha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToaNhaRepository extends JpaRepository<ToaNha, Integer> {

    /**
     * Tìm tòa nhà theo tên
     */
    Optional<ToaNha> findByTenToaNha(String tenToaNha);

    /**
     * Kiểm tra tên tòa nhà đã tồn tại chưa
     */
    boolean existsByTenToaNha(String tenToaNha);

    /**
     * Tìm tòa nhà theo tên (có phân trang)
     */
    Page<ToaNha> findByTenToaNhaContainingIgnoreCase(String tenToaNha, Pageable pageable);
}
