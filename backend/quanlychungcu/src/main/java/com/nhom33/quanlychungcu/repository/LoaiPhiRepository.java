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

@Repository
public interface LoaiPhiRepository extends JpaRepository<LoaiPhi, Integer> {
    
    Optional<LoaiPhi> findByTenLoaiPhi(String tenLoaiPhi);
    
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
}

