package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Integer> {
    
    List<ChiTietHoaDon> findByHoaDonId(Integer idHoaDon);
    
    /**
     * Tìm chi tiết hóa đơn theo hóa đơn và loại phí.
     */
    Optional<ChiTietHoaDon> findByHoaDonIdAndLoaiPhiId(Integer hoaDonId, Integer loaiPhiId);
    
    /**
     * Xóa chi tiết hóa đơn theo hóa đơn.
     */
    void deleteByHoaDonId(Integer hoaDonId);
}

