package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    
    List<HoaDon> findByHoGiaDinhId(Integer idHoGiaDinh);
    
    /**
     * Lấy danh sách hóa đơn theo hộ gia đình với JOIN FETCH để tránh LazyInitializationException.
     */
    @Query("SELECT h FROM HoaDon h " +
           "LEFT JOIN FETCH h.hoGiaDinh hgd " +
           "LEFT JOIN FETCH hgd.toaNha " +
           "LEFT JOIN FETCH h.dotThu " +
           "WHERE h.hoGiaDinh.id = :idHoGiaDinh")
    List<HoaDon> findByHoGiaDinhIdWithDetails(@Param("idHoGiaDinh") Integer idHoGiaDinh);

    /**
     * Pageable version - cần dùng countQuery riêng.
     */
    @Query(value = "SELECT h FROM HoaDon h " +
           "LEFT JOIN FETCH h.hoGiaDinh hgd " +
           "LEFT JOIN FETCH hgd.toaNha " +
           "LEFT JOIN FETCH h.dotThu " +
           "WHERE h.hoGiaDinh.id = :idHoGiaDinh",
           countQuery = "SELECT COUNT(h) FROM HoaDon h WHERE h.hoGiaDinh.id = :idHoGiaDinh")
    Page<HoaDon> findByHoGiaDinhIdWithDetails(@Param("idHoGiaDinh") Integer idHoGiaDinh, Pageable pageable);
    
    Page<HoaDon> findByHoGiaDinhId(Integer idHoGiaDinh, Pageable pageable);
    
    List<HoaDon> findByDotThuId(Integer idDotThu);
    
    @Query("SELECT h FROM HoaDon h JOIN FETCH h.hoGiaDinh WHERE h.dotThu.id = :idDotThu")
    List<HoaDon> findByDotThuIdWithHoGiaDinh(@Param("idDotThu") Integer idDotThu);
    
    Page<HoaDon> findByDotThuId(Integer idDotThu, Pageable pageable);
    
    Optional<HoaDon> findByHoGiaDinhIdAndDotThuId(Integer idHoGiaDinh, Integer idDotThu);
    
    /**
     * Lấy hóa đơn theo Hộ gia đình và Đợt thu với JOIN FETCH.
     * Dùng cho API thanh toán online.
     */
    @Query("SELECT h FROM HoaDon h " +
           "LEFT JOIN FETCH h.hoGiaDinh hgd " +
           "LEFT JOIN FETCH hgd.toaNha " +
           "LEFT JOIN FETCH h.dotThu " +
           "LEFT JOIN FETCH h.danhSachChiTiet ct " +
           "LEFT JOIN FETCH ct.loaiPhi " +
           "WHERE h.hoGiaDinh.id = :idHoGiaDinh AND h.dotThu.id = :idDotThu")
    Optional<HoaDon> findByHoGiaDinhIdAndDotThuIdWithDetails(
            @Param("idHoGiaDinh") Integer idHoGiaDinh,
            @Param("idDotThu") Integer idDotThu);
    
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

