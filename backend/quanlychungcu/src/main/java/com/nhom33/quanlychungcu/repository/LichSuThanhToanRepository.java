package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.LichSuThanhToan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Integer> {
    
    List<LichSuThanhToan> findByHoaDonId(Integer idHoaDon);
    
    Page<LichSuThanhToan> findByHoaDonId(Integer idHoaDon, Pageable pageable);
    
    @Query("SELECT l FROM LichSuThanhToan l WHERE " +
           "(:idHoaDon IS NULL OR l.hoaDon.id = :idHoaDon) AND " +
           "(:hinhThuc IS NULL OR l.hinhThuc = :hinhThuc) AND " +
           "(:ngayBatDau IS NULL OR l.ngayNop >= :ngayBatDau) AND " +
           "(:ngayKetThuc IS NULL OR l.ngayNop <= :ngayKetThuc)")
    Page<LichSuThanhToan> search(@Param("idHoaDon") Integer idHoaDon,
                                 @Param("hinhThuc") String hinhThuc,
                                 @Param("ngayBatDau") LocalDateTime ngayBatDau,
                                 @Param("ngayKetThuc") LocalDateTime ngayKetThuc,
                                 Pageable pageable);
    
    @Query("SELECT SUM(l.soTien) FROM LichSuThanhToan l WHERE l.hoaDon.id = :idHoaDon")
    java.math.BigDecimal sumSoTienByHoaDonId(@Param("idHoaDon") Integer idHoaDon);
}

