package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.TamTru;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TamTruRepository extends JpaRepository<TamTru, Integer> {

    /**
     * Tìm các TamTru theo họ tên, không phân biệt hoa thường
     * Sử dụng cho searchByName trong TamTruService
     */
    Page<TamTru> findByHoTenContainingIgnoreCase(String hoTen, Pageable pageable);

    /**
     * Tìm các TamTru theo số CCCD chính xác
     */
    Page<TamTru> findBySoCCCD(String soCCCD, Pageable pageable);
}
