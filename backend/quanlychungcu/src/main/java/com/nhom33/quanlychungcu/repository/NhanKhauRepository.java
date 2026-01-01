package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.NhanKhau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanKhauRepository extends JpaRepository<NhanKhau, Integer> {

    /**
     * Tìm nhân khẩu theo số CCCD (unique)
     */
    Optional<NhanKhau> findBySoCCCD(String soCCCD);

    /**
     * Kiểm tra số CCCD đã tồn tại chưa
     */
    boolean existsBySoCCCD(String soCCCD);

    /**
     * Tìm nhân khẩu theo họ tên
     */
    Page<NhanKhau> findByHoTenContainingIgnoreCase(String hoTen, Pageable pageable);

    /**
     * Tìm tất cả nhân khẩu trong một hộ gia đình
     */
    List<NhanKhau> findByHoGiaDinhId(Integer idHoGiaDinh);

    /**
     * Tìm nhân khẩu theo hộ gia đình (có phân trang)
     */
    Page<NhanKhau> findByHoGiaDinhId(Integer idHoGiaDinh, Pageable pageable);

    /**
     * Tìm chủ hộ của một hộ gia đình
     */
    @Query("SELECT n FROM NhanKhau n WHERE n.hoGiaDinh.id = :idHoGiaDinh AND n.laChuHo = true")
    Optional<NhanKhau> findChuHoByHoGiaDinhId(@Param("idHoGiaDinh") Integer idHoGiaDinh);

    /**
     * Tìm nhân khẩu theo giới tính
     */
    Page<NhanKhau> findByGioiTinh(String gioiTinh, Pageable pageable);

    /**
     * Tìm nhân khẩu theo trạng thái
     */
    Page<NhanKhau> findByTrangThai(String trangThai, Pageable pageable);

    /**
     * Đếm số nhân khẩu trong một hộ gia đình
     */
    long countByHoGiaDinhId(Integer idHoGiaDinh);

    /**
     * Tìm kiếm đa điều kiện
     */
    @Query("SELECT n FROM NhanKhau n WHERE " +
           "(:hoTen IS NULL OR n.hoTen LIKE %:hoTen%) AND " +
           "(:soCCCD IS NULL OR n.soCCCD = :soCCCD) AND " +
           "(:gioiTinh IS NULL OR n.gioiTinh = :gioiTinh) AND " +
           "(:trangThai IS NULL OR n.trangThai = :trangThai) AND " +
           "(:idHoGiaDinh IS NULL OR n.hoGiaDinh.id = :idHoGiaDinh)")
    Page<NhanKhau> search(@Param("hoTen") String hoTen,
                          @Param("soCCCD") String soCCCD,
                          @Param("gioiTinh") String gioiTinh,
                          @Param("trangThai") String trangThai,
                          @Param("idHoGiaDinh") Integer idHoGiaDinh,
                          Pageable pageable);

    /**
     * Đếm số nhân khẩu theo giới tính
     */
    long countByGioiTinh(String gioiTinh);

    /**
     * Lấy danh sách nhân khẩu là chủ hộ
     */
    Page<NhanKhau> findByLaChuHo(Boolean laChuHo, Pageable pageable);
}
