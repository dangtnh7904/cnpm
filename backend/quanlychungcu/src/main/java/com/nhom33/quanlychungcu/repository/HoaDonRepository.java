package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    
    List<HoaDon> findByHoGiaDinhId(Integer idHoGiaDinh);
    
    Page<HoaDon> findByHoGiaDinhId(Integer idHoGiaDinh, Pageable pageable);
    
    List<HoaDon> findByDotThuId(Integer idDotThu);
    
    Page<HoaDon> findByDotThuId(Integer idDotThu, Pageable pageable);
    
    Optional<HoaDon> findByHoGiaDinhIdAndDotThuId(Integer idHoGiaDinh, Integer idDotThu);
    
    Page<HoaDon> findByTrangThai(String trangThai, Pageable pageable);
    
    @Query("SELECT h FROM HoaDon h WHERE " +
           "(:idHoGiaDinh IS NULL OR h.hoGiaDinh.id = :idHoGiaDinh) AND " +
           "(:idDotThu IS NULL OR h.dotThu.id = :idDotThu) AND " +
           "(:trangThai IS NULL OR h.trangThai = :trangThai)")
    Page<HoaDon> search(@Param("idHoGiaDinh") Integer idHoGiaDinh,
                      @Param("idDotThu") Integer idDotThu,
                      @Param("trangThai") String trangThai,
                      Pageable pageable);
    
    @Query("SELECT SUM(h.tongTienPhaiThu) FROM HoaDon h WHERE h.dotThu.id = :idDotThu")
    java.math.BigDecimal sumTongTienPhaiThuByDotThu(@Param("idDotThu") Integer idDotThu);
    
    @Query("SELECT SUM(h.soTienDaDong) FROM HoaDon h WHERE h.dotThu.id = :idDotThu")
    java.math.BigDecimal sumSoTienDaDongByDotThu(@Param("idDotThu") Integer idDotThu);
    
    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.dotThu.id = :idDotThu AND h.trangThai = :trangThai")
    Long countByDotThuAndTrangThai(@Param("idDotThu") Integer idDotThu, @Param("trangThai") String trangThai);
}

