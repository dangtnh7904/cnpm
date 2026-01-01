package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoGiaDinhRepository extends JpaRepository<HoGiaDinh, Integer> {

    /**
     * Tìm hộ gia đình theo mã hộ (unique)
     */
    Optional<HoGiaDinh> findByMaHoGiaDinh(String maHoGiaDinh);

    /**
     * Kiểm tra mã hộ đã tồn tại chưa (toàn hệ thống)
     */
    boolean existsByMaHoGiaDinh(String maHoGiaDinh);

    /**
     * Kiểm tra cặp (MaHoGiaDinh, ID_ToaNha) đã tồn tại chưa.
     * Unique constraint: Mã hộ có thể trùng ở các tòa khác nhau, 
     * nhưng không được trùng trong cùng một tòa nhà.
     */
    boolean existsByMaHoGiaDinhAndToaNhaId(String maHoGiaDinh, Integer toaNhaId);

    /**
     * Kiểm tra cặp (MaHoGiaDinh, ID_ToaNha) đã tồn tại (ngoại trừ ID hiện tại).
     * Dùng cho update khi cần check trùng nhưng loại trừ chính bản ghi đang sửa.
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM HoGiaDinh h " +
           "WHERE h.maHoGiaDinh = :maHo AND h.toaNha.id = :toaNhaId AND h.id <> :excludeId")
    boolean existsByMaHoGiaDinhAndToaNhaIdExcludingId(
            @Param("maHo") String maHoGiaDinh, 
            @Param("toaNhaId") Integer toaNhaId, 
            @Param("excludeId") Integer excludeId);

    /**
     * Tìm hộ gia đình theo tên chủ hộ (có phân trang)
     */
    Page<HoGiaDinh> findByTenChuHoContainingIgnoreCase(String tenChuHo, Pageable pageable);

    /**
     * Tìm hộ gia đình theo số căn hộ
     */
    Page<HoGiaDinh> findBySoCanHo(String soCanHo, Pageable pageable);

    /**
     * Tìm hộ gia đình theo tầng
     */
    Page<HoGiaDinh> findBySoTang(Integer soTang, Pageable pageable);

    /**
     * Tìm hộ gia đình theo trạng thái
     */
    Page<HoGiaDinh> findByTrangThai(String trangThai, Pageable pageable);

    /**
     * Tìm hộ gia đình theo tầng và số căn hộ
     */
    @Query("SELECT h FROM HoGiaDinh h WHERE h.soTang = :tang AND h.soCanHo = :canHo")
    Optional<HoGiaDinh> findByTangAndCanHo(@Param("tang") Integer soTang, @Param("canHo") String soCanHo);

    /**
     * Tìm kiếm đa điều kiện
     */
    @Query("SELECT h FROM HoGiaDinh h WHERE " +
           "(:maHo IS NULL OR h.maHoGiaDinh LIKE %:maHo%) AND " +
           "(:tenChuHo IS NULL OR h.tenChuHo LIKE %:tenChuHo%) AND " +
           "(:soCanHo IS NULL OR h.soCanHo = :soCanHo) AND " +
           "(:trangThai IS NULL OR h.trangThai = :trangThai)")
    Page<HoGiaDinh> search(@Param("maHo") String maHoGiaDinh,
                           @Param("tenChuHo") String tenChuHo,
                           @Param("soCanHo") String soCanHo,
                           @Param("trangThai") String trangThai,
                           Pageable pageable);

    /**
     * Đếm số hộ gia đình theo trạng thái
     */
    long countByTrangThai(String trangThai);

    /**
     * Lấy danh sách hộ gia đình có diện tích lớn hơn
     */
    Page<HoGiaDinh> findByDienTichGreaterThan(Double dienTich, Pageable pageable);
}
