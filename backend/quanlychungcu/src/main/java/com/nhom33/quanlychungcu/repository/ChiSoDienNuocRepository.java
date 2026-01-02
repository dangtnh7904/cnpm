package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.ChiSoDienNuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiSoDienNuocRepository extends JpaRepository<ChiSoDienNuoc, Integer> {

    /**
     * Lấy chỉ số theo hộ gia đình, đợt thu, loại phí.
     */
    Optional<ChiSoDienNuoc> findByHoGiaDinhIdAndDotThuIdAndLoaiPhiId(
            Integer hoGiaDinhId, Integer dotThuId, Integer loaiPhiId);

    /**
     * Lấy tất cả chỉ số trong một đợt thu theo loại phí.
     */
    List<ChiSoDienNuoc> findByDotThuIdAndLoaiPhiId(Integer dotThuId, Integer loaiPhiId);

    /**
     * Lấy tất cả chỉ số trong một đợt thu.
     */
    List<ChiSoDienNuoc> findByDotThuId(Integer dotThuId);

    /**
     * Lấy chỉ số mới nhất của hộ gia đình theo loại phí (để làm chỉ số cũ cho tháng sau).
     * Sắp xếp theo ngày chốt giảm dần, lấy bản ghi đầu tiên.
     */
    @Query("SELECT c FROM ChiSoDienNuoc c " +
           "WHERE c.hoGiaDinh.id = :hoGiaDinhId " +
           "AND c.loaiPhi.id = :loaiPhiId " +
           "AND c.chiSoMoi IS NOT NULL " +
           "AND c.dotThu.id != :excludeDotThuId " +
           "ORDER BY c.ngayChot DESC")
    List<ChiSoDienNuoc> findLatestByHoGiaDinhAndLoaiPhi(
            @Param("hoGiaDinhId") Integer hoGiaDinhId,
            @Param("loaiPhiId") Integer loaiPhiId,
            @Param("excludeDotThuId") Integer excludeDotThuId);

    /**
     * Lấy chỉ số mới nhất của hộ gia đình theo loại phí (không loại trừ đợt nào).
     */
    @Query("SELECT c FROM ChiSoDienNuoc c " +
           "WHERE c.hoGiaDinh.id = :hoGiaDinhId " +
           "AND c.loaiPhi.id = :loaiPhiId " +
           "AND c.chiSoMoi IS NOT NULL " +
           "ORDER BY c.ngayChot DESC")
    List<ChiSoDienNuoc> findLatestByHoGiaDinhAndLoaiPhi(
            @Param("hoGiaDinhId") Integer hoGiaDinhId,
            @Param("loaiPhiId") Integer loaiPhiId);

    /**
     * Đếm số hộ chưa nhập chỉ số trong đợt thu.
     */
    @Query("SELECT COUNT(c) FROM ChiSoDienNuoc c " +
           "WHERE c.dotThu.id = :dotThuId " +
           "AND c.loaiPhi.id = :loaiPhiId " +
           "AND c.chiSoMoi IS NULL")
    long countChuaNhap(@Param("dotThuId") Integer dotThuId, @Param("loaiPhiId") Integer loaiPhiId);

    /**
     * Đếm số hộ đã nhập chỉ số trong đợt thu.
     */
    @Query("SELECT COUNT(c) FROM ChiSoDienNuoc c " +
           "WHERE c.dotThu.id = :dotThuId " +
           "AND c.loaiPhi.id = :loaiPhiId " +
           "AND c.chiSoMoi IS NOT NULL")
    long countDaNhap(@Param("dotThuId") Integer dotThuId, @Param("loaiPhiId") Integer loaiPhiId);

    /**
     * Lấy danh sách chỉ số theo tòa nhà.
     */
    @Query("SELECT c FROM ChiSoDienNuoc c " +
           "WHERE c.dotThu.id = :dotThuId " +
           "AND c.loaiPhi.id = :loaiPhiId " +
           "AND c.hoGiaDinh.toaNha.id = :toaNhaId")
    List<ChiSoDienNuoc> findByDotThuAndLoaiPhiAndToaNha(
            @Param("dotThuId") Integer dotThuId,
            @Param("loaiPhiId") Integer loaiPhiId,
            @Param("toaNhaId") Integer toaNhaId);

    /**
     * Kiểm tra đã tồn tại bản ghi chưa.
     */
    boolean existsByHoGiaDinhIdAndDotThuIdAndLoaiPhiId(
            Integer hoGiaDinhId, Integer dotThuId, Integer loaiPhiId);
}
