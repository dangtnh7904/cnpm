package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.TamVang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TamVangRepository extends JpaRepository<TamVang, Integer> {

    // Phương thức search theo 'noiDen' (không phân biệt hoa thường)
    Page<TamVang> findByNoiDenContainingIgnoreCase(String noiDen, Pageable pageable);
    
    // Đếm số bản ghi tạm vắng của một nhân khẩu
    long countByNhanKhauId(Integer nhanKhauId);
    
    // Tìm tất cả bản ghi tạm vắng của một nhân khẩu
    Page<TamVang> findByNhanKhauId(Integer nhanKhauId, Pageable pageable);
}
