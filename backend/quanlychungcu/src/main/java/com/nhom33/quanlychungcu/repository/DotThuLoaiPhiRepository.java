package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.DotThuLoaiPhi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DotThuLoaiPhiRepository extends JpaRepository<DotThuLoaiPhi, Integer> {

    /**
     * Lấy tất cả loại phí trong một đợt thu.
     */
    List<DotThuLoaiPhi> findByDotThuId(Integer dotThuId);

    /**
     * Kiểm tra loại phí đã tồn tại trong đợt thu chưa.
     */
    boolean existsByDotThuIdAndLoaiPhiId(Integer dotThuId, Integer loaiPhiId);

    /**
     * Tìm config theo đợt thu và loại phí.
     */
    Optional<DotThuLoaiPhi> findByDotThuIdAndLoaiPhiId(Integer dotThuId, Integer loaiPhiId);

    /**
     * Xóa config theo đợt thu và loại phí.
     */
    void deleteByDotThuIdAndLoaiPhiId(Integer dotThuId, Integer loaiPhiId);

    /**
     * Đếm số loại phí trong đợt thu.
     */
    long countByDotThuId(Integer dotThuId);

    /**
     * Lấy danh sách loại phí ID trong đợt thu.
     */
    @Query("SELECT dlp.loaiPhi.id FROM DotThuLoaiPhi dlp WHERE dlp.dotThu.id = :dotThuId")
    List<Integer> findLoaiPhiIdsByDotThuId(@Param("dotThuId") Integer dotThuId);
}
