package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.LoaiPhi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: LoaiPhi
 * 
 * MULTI-TENANCY v4.1:
 * - Loại phí thuộc về Manager (nguoiQuanLy)
 * - toaNha = NULL: phí CHUNG của Manager (áp dụng cho tất cả tòa của Manager)
 * - toaNha != NULL: phí RIÊNG chỉ cho tòa đó
 */
@Repository
public interface LoaiPhiRepository extends JpaRepository<LoaiPhi, Integer> {
    
    /**
     * Tìm loại phí theo tên (lấy bản ghi đầu tiên nếu có nhiều).
     */
    Optional<LoaiPhi> findFirstByTenLoaiPhi(String tenLoaiPhi);
    
    Page<LoaiPhi> findByDangHoatDong(Boolean dangHoatDong, Pageable pageable);
    
    Page<LoaiPhi> findByLoaiThu(String loaiThu, Pageable pageable);
    
    @Query("SELECT l FROM LoaiPhi l WHERE " +
           "(:tenLoaiPhi IS NULL OR l.tenLoaiPhi LIKE %:tenLoaiPhi%) AND " +
           "(:loaiThu IS NULL OR l.loaiThu = :loaiThu) AND " +
           "(:dangHoatDong IS NULL OR l.dangHoatDong = :dangHoatDong)")
    Page<LoaiPhi> search(@Param("tenLoaiPhi") String tenLoaiPhi,
                        @Param("loaiThu") String loaiThu,
                        @Param("dangHoatDong") Boolean dangHoatDong,
                        Pageable pageable);
    
    List<LoaiPhi> findByDangHoatDongTrue();
    
    // ===== Multi-tenancy v4.1: Loại phí thuộc về Manager =====
    
    /**
     * Lấy tất cả loại phí của một Manager.
     * Bao gồm cả phí CHUNG (toaNha = NULL) và phí RIÊNG của các tòa.
     */
    @Query("SELECT l FROM LoaiPhi l WHERE l.nguoiQuanLy.id = :managerId")
    Page<LoaiPhi> findByNguoiQuanLyId(@Param("managerId") Integer managerId, Pageable pageable);
    
    /**
     * Lấy tất cả loại phí CHUNG của một Manager (không gắn tòa cụ thể).
     * Phí chung: toaNha = NULL
     */
    @Query("SELECT l FROM LoaiPhi l WHERE l.nguoiQuanLy.id = :managerId AND l.toaNha IS NULL")
    Page<LoaiPhi> findPhiChungByNguoiQuanLy(@Param("managerId") Integer managerId, Pageable pageable);
    
    /**
     * Lấy tất cả loại phí CHUNG đang hoạt động của một Manager.
     */
    @Query("SELECT l FROM LoaiPhi l WHERE l.nguoiQuanLy.id = :managerId AND l.toaNha IS NULL AND l.dangHoatDong = true")
    List<LoaiPhi> findPhiChungActiveByNguoiQuanLy(@Param("managerId") Integer managerId);
    
    /**
     * Lấy tất cả loại phí áp dụng cho một tòa nhà cụ thể.
     * Bao gồm: phí CHUNG của Manager + phí RIÊNG của tòa đó.
     */
    @Query("SELECT l FROM LoaiPhi l WHERE l.nguoiQuanLy.id = :managerId AND " +
           "(l.toaNha IS NULL OR l.toaNha.id = :toaNhaId)")
    Page<LoaiPhi> findByNguoiQuanLyAndToaNha(@Param("managerId") Integer managerId, 
                                             @Param("toaNhaId") Integer toaNhaId, 
                                             Pageable pageable);
    
    /**
     * Lấy tất cả loại phí đang hoạt động áp dụng cho một tòa nhà.
     */
    @Query("SELECT l FROM LoaiPhi l WHERE l.nguoiQuanLy.id = :managerId AND " +
           "(l.toaNha IS NULL OR l.toaNha.id = :toaNhaId) AND l.dangHoatDong = true")
    List<LoaiPhi> findActiveByNguoiQuanLyAndToaNha(@Param("managerId") Integer managerId, 
                                                    @Param("toaNhaId") Integer toaNhaId);
    
    /**
     * Tìm kiếm loại phí của Manager với các tiêu chí.
     */
    @Query("SELECT l FROM LoaiPhi l WHERE " +
           "l.nguoiQuanLy.id = :managerId AND " +
           "(:tenLoaiPhi IS NULL OR l.tenLoaiPhi LIKE %:tenLoaiPhi%) AND " +
           "(:loaiThu IS NULL OR l.loaiThu = :loaiThu) AND " +
           "(:dangHoatDong IS NULL OR l.dangHoatDong = :dangHoatDong)")
    Page<LoaiPhi> searchByNguoiQuanLy(@Param("managerId") Integer managerId,
                                      @Param("tenLoaiPhi") String tenLoaiPhi,
                                      @Param("loaiThu") String loaiThu,
                                      @Param("dangHoatDong") Boolean dangHoatDong,
                                      Pageable pageable);
    
    // ===== Legacy: Tìm theo tòa nhà (cho backward compatibility) =====
    
    /**
     * Lấy danh sách loại phí của các tòa nhà chỉ định.
     * @deprecated Sử dụng findByNguoiQuanLyId thay thế
     */
    @Query("SELECT l FROM LoaiPhi l WHERE l.toaNha.id IN :toaNhaIds")
    Page<LoaiPhi> findByToaNhaIdIn(@Param("toaNhaIds") List<Integer> toaNhaIds, Pageable pageable);
    
    /**
     * Lấy danh sách loại phí đang hoạt động của các tòa nhà chỉ định.
     * @deprecated Sử dụng findActiveByNguoiQuanLyAndToaNha thay thế
     */
    @Query("SELECT l FROM LoaiPhi l WHERE l.dangHoatDong = true AND l.toaNha.id IN :toaNhaIds")
    List<LoaiPhi> findByDangHoatDongTrueAndToaNhaIdIn(@Param("toaNhaIds") List<Integer> toaNhaIds);
    
    /**
     * Tìm kiếm loại phí với multi-tenancy.
     * @deprecated Sử dụng searchByNguoiQuanLy thay thế
     */
    @Query("SELECT l FROM LoaiPhi l WHERE " +
           "l.toaNha.id IN :toaNhaIds AND " +
           "(:tenLoaiPhi IS NULL OR l.tenLoaiPhi LIKE %:tenLoaiPhi%) AND " +
           "(:loaiThu IS NULL OR l.loaiThu = :loaiThu) AND " +
           "(:dangHoatDong IS NULL OR l.dangHoatDong = :dangHoatDong)")
    Page<LoaiPhi> searchByToaNhaIds(@Param("toaNhaIds") List<Integer> toaNhaIds,
                                   @Param("tenLoaiPhi") String tenLoaiPhi,
                                   @Param("loaiThu") String loaiThu,
                                   @Param("dangHoatDong") Boolean dangHoatDong,
                                   Pageable pageable);
}

