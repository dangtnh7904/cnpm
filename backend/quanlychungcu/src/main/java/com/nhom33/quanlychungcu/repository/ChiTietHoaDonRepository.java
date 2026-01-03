package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Integer> {
    
    List<ChiTietHoaDon> findByHoaDonId(Integer idHoaDon);
    
    /**
     * Tìm chi tiết hóa đơn với LoaiPhi eager fetch (tránh LazyInitializationException).
     */
    @Query("SELECT ct FROM ChiTietHoaDon ct JOIN FETCH ct.loaiPhi WHERE ct.hoaDon.id = :idHoaDon")
    List<ChiTietHoaDon> findByHoaDonIdWithLoaiPhi(@Param("idHoaDon") Integer idHoaDon);
    
    /**
     * Tìm chi tiết hóa đơn theo hóa đơn và loại phí.
     */
    Optional<ChiTietHoaDon> findByHoaDonIdAndLoaiPhiId(Integer hoaDonId, Integer loaiPhiId);
    
    /**
     * Xóa chi tiết hóa đơn theo hóa đơn.
     */
    void deleteByHoaDonId(Integer hoaDonId);
}

