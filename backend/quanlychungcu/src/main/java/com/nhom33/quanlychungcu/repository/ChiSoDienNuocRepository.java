package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.ChiSoDienNuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: Chỉ số Điện Nước.
 * 
 * LOGIC NGHIỆP VỤ MỚI (Tách rời ghi số và thu tiền):
 * - Ghi chỉ số theo Tháng/Năm, không phụ thuộc Đợt thu
 * - Khi tạo Đợt thu có phí Điện/Nước -> Query theo Tháng/Năm để tính tiền
 */
@Repository
public interface ChiSoDienNuocRepository extends JpaRepository<ChiSoDienNuoc, Integer> {

    /**
     * Lấy chỉ số theo hộ gia đình, loại phí, tháng, năm.
     */
    Optional<ChiSoDienNuoc> findByHoGiaDinhIdAndLoaiPhiIdAndThangAndNam(
            Integer hoGiaDinhId, Integer loaiPhiId, Integer thang, Integer nam);

    /**
     * Lấy tất cả chỉ số trong một tháng/năm theo loại phí.
     */
    List<ChiSoDienNuoc> findByThangAndNamAndLoaiPhiId(Integer thang, Integer nam, Integer loaiPhiId);

    /**
     * Lấy tất cả chỉ số trong một tháng/năm.
     */
    List<ChiSoDienNuoc> findByThangAndNam(Integer thang, Integer nam);

    /**
     * Lấy chỉ số của tháng trước (để tính tiêu thụ).
     * @param hoGiaDinhId ID hộ gia đình
     * @param loaiPhiId ID loại phí (Điện/Nước)
     * @param thang Tháng hiện tại
     * @param nam Năm hiện tại
     * @return Chỉ số tháng trước (T-1)
     */
    @Query("SELECT c FROM ChiSoDienNuoc c " +
           "WHERE c.hoGiaDinh.id = :hoGiaDinhId " +
           "AND c.loaiPhi.id = :loaiPhiId " +
           "AND ((c.nam = :nam AND c.thang = :thang - 1) OR " +
           "     (c.thang = 12 AND c.nam = :nam - 1 AND :thang = 1))")
    Optional<ChiSoDienNuoc> findPreviousMonth(
            @Param("hoGiaDinhId") Integer hoGiaDinhId,
            @Param("loaiPhiId") Integer loaiPhiId,
            @Param("thang") Integer thang,
            @Param("nam") Integer nam);

    /**
     * Lấy chỉ số mới nhất của hộ gia đình theo loại phí.
     * Sắp xếp theo Năm, Tháng giảm dần.
     */
    @Query("SELECT c FROM ChiSoDienNuoc c " +
           "WHERE c.hoGiaDinh.id = :hoGiaDinhId " +
           "AND c.loaiPhi.id = :loaiPhiId " +
           "ORDER BY c.nam DESC, c.thang DESC")
    List<ChiSoDienNuoc> findLatestByHoGiaDinhAndLoaiPhi(
            @Param("hoGiaDinhId") Integer hoGiaDinhId,
            @Param("loaiPhiId") Integer loaiPhiId);

    /**
     * Đếm số hộ đã ghi chỉ số trong tháng/năm theo tòa nhà.
     */
    @Query("SELECT COUNT(c) FROM ChiSoDienNuoc c " +
           "WHERE c.thang = :thang AND c.nam = :nam " +
           "AND c.loaiPhi.id = :loaiPhiId " +
           "AND c.hoGiaDinh.toaNha.id = :toaNhaId")
    long countByThangNamAndLoaiPhiAndToaNha(
            @Param("thang") Integer thang,
            @Param("nam") Integer nam,
            @Param("loaiPhiId") Integer loaiPhiId,
            @Param("toaNhaId") Integer toaNhaId);

    /**
     * Lấy danh sách chỉ số theo tháng/năm và tòa nhà.
     */
    @Query("SELECT c FROM ChiSoDienNuoc c " +
           "WHERE c.thang = :thang AND c.nam = :nam " +
           "AND c.loaiPhi.id = :loaiPhiId " +
           "AND c.hoGiaDinh.toaNha.id = :toaNhaId")
    List<ChiSoDienNuoc> findByThangNamAndLoaiPhiAndToaNha(
            @Param("thang") Integer thang,
            @Param("nam") Integer nam,
            @Param("loaiPhiId") Integer loaiPhiId,
            @Param("toaNhaId") Integer toaNhaId);

    /**
     * Kiểm tra đã tồn tại bản ghi chưa.
     */
    boolean existsByHoGiaDinhIdAndLoaiPhiIdAndThangAndNam(
            Integer hoGiaDinhId, Integer loaiPhiId, Integer thang, Integer nam);

    /**
     * Lấy tất cả chỉ số của một hộ gia đình.
     */
    List<ChiSoDienNuoc> findByHoGiaDinhIdOrderByNamDescThangDesc(Integer hoGiaDinhId);
}
