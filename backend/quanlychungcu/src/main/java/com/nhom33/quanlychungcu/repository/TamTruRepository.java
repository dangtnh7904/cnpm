package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.TamTru;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TamTruRepository extends JpaRepository<TamTru, Integer> {

    /**
     * Tìm Tạm trú theo tên nhân khẩu (không phân biệt hoa thường).
     */
    Page<TamTru> findByNhanKhau_HoTenContainingIgnoreCase(String hoTen, Pageable pageable);

    /**
     * Tìm Tạm trú theo ID hộ gia đình (thông qua NhanKhau).
     */
    Page<TamTru> findByNhanKhau_HoGiaDinh_Id(Integer hoGiaDinhId, Pageable pageable);

    /**
     * Tìm Tạm trú theo ID nhân khẩu.
     */
    Page<TamTru> findByNhanKhauId(Integer nhanKhauId, Pageable pageable);

    /**
     * Đếm số bản ghi tạm trú của một nhân khẩu.
     */
    long countByNhanKhauId(Integer nhanKhauId);
}
