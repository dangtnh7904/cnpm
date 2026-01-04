package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToaNhaRepository extends JpaRepository<ToaNha, Integer> {

    /**
     * Tìm tòa nhà theo tên
     */
    Optional<ToaNha> findByTenToaNha(String tenToaNha);

    /**
     * Kiểm tra tên tòa nhà đã tồn tại chưa
     */
    boolean existsByTenToaNha(String tenToaNha);

    /**
     * Tìm tòa nhà theo tên (có phân trang)
     */
    Page<ToaNha> findByTenToaNhaContainingIgnoreCase(String tenToaNha, Pageable pageable);

    /**
     * Tìm tất cả tòa nhà theo người quản lý (Multi-tenancy)
     */
    List<ToaNha> findByNguoiQuanLy(UserAccount nguoiQuanLy);

    /**
     * Tìm tòa nhà theo ID người quản lý
     */
    List<ToaNha> findByNguoiQuanLyId(Integer nguoiQuanLyId);

    /**
     * Tìm tòa nhà theo người quản lý (có phân trang)
     */
    Page<ToaNha> findByNguoiQuanLy(UserAccount nguoiQuanLy, Pageable pageable);

    /**
     * Tìm tòa nhà theo người quản lý và tên (có phân trang)
     */
    Page<ToaNha> findByNguoiQuanLyAndTenToaNhaContainingIgnoreCase(
        UserAccount nguoiQuanLy, String tenToaNha, Pageable pageable);
}
