package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.BangGiaDichVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository: BangGiaDichVu - Bảng giá dịch vụ theo tòa nhà.
 * 
 * CHỨC NĂNG:
 * - Lấy giá riêng cho từng loại phí tại từng tòa nhà.
 * - Hỗ trợ logic ưu tiên giá: BangGiaDichVu > LoaiPhi.DonGia.
 */
@Repository
public interface BangGiaDichVuRepository extends JpaRepository<BangGiaDichVu, Integer> {

    // ===== Tìm kiếm cơ bản =====

    /**
     * Tìm bảng giá theo loại phí và tòa nhà.
     * Dùng cho việc lấy giá ưu tiên khi tính phí.
     */
    Optional<BangGiaDichVu> findByLoaiPhiIdAndToaNhaId(Integer loaiPhiId, Integer toaNhaId);

    /**
     * Tìm tất cả bảng giá của một loại phí.
     * Dùng khi xem/quản lý giá của một loại phí tại các tòa nhà.
     */
    List<BangGiaDichVu> findByLoaiPhiId(Integer loaiPhiId);

    /**
     * Tìm tất cả bảng giá của một tòa nhà.
     * Dùng khi xem/quản lý giá của các loại phí tại một tòa nhà.
     */
    List<BangGiaDichVu> findByToaNhaId(Integer toaNhaId);

    // ===== Kiểm tra tồn tại =====

    /**
     * Kiểm tra đã có bảng giá cho cặp (loại phí, tòa nhà) chưa.
     */
    boolean existsByLoaiPhiIdAndToaNhaId(Integer loaiPhiId, Integer toaNhaId);

    // ===== Query tùy chỉnh =====

    /**
     * Lấy đơn giá của một loại phí tại một tòa nhà.
     * Trả về null nếu không có bảng giá riêng (sẽ dùng giá mặc định).
     */
    @Query("SELECT b.donGia FROM BangGiaDichVu b " +
           "WHERE b.loaiPhi.id = :loaiPhiId AND b.toaNha.id = :toaNhaId")
    Optional<BigDecimal> findDonGiaByLoaiPhiAndToaNha(
            @Param("loaiPhiId") Integer loaiPhiId,
            @Param("toaNhaId") Integer toaNhaId);

    /**
     * Lấy tất cả bảng giá với thông tin đầy đủ (fetch eager).
     */
    @Query("SELECT b FROM BangGiaDichVu b " +
           "JOIN FETCH b.loaiPhi l " +
           "JOIN FETCH b.toaNha t " +
           "ORDER BY t.tenToaNha, l.tenLoaiPhi")
    List<BangGiaDichVu> findAllWithDetails();

    /**
     * Lấy tất cả bảng giá của một tòa nhà với thông tin đầy đủ.
     */
    @Query("SELECT b FROM BangGiaDichVu b " +
           "JOIN FETCH b.loaiPhi l " +
           "WHERE b.toaNha.id = :toaNhaId " +
           "ORDER BY l.tenLoaiPhi")
    List<BangGiaDichVu> findByToaNhaIdWithDetails(@Param("toaNhaId") Integer toaNhaId);

    /**
     * Xóa tất cả bảng giá của một loại phí.
     * Dùng khi xóa loại phí hoặc reset giá.
     */
    void deleteByLoaiPhiId(Integer loaiPhiId);

    /**
     * Xóa tất cả bảng giá của một tòa nhà.
     * Dùng khi xóa tòa nhà hoặc reset giá.
     */
    void deleteByToaNhaId(Integer toaNhaId);

    /**
     * Xóa bảng giá theo cặp (loại phí, tòa nhà).
     */
    void deleteByLoaiPhiIdAndToaNhaId(Integer loaiPhiId, Integer toaNhaId);
}
