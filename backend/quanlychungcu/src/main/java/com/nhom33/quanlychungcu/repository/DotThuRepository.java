package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.DotThu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DotThuRepository extends JpaRepository<DotThu, Integer> {
    
    Page<DotThu> findByLoaiDotThu(String loaiDotThu, Pageable pageable);
    
    @Query("SELECT d FROM DotThu d WHERE " +
           "(:tenDotThu IS NULL OR d.tenDotThu LIKE %:tenDotThu%) AND " +
           "(:loaiDotThu IS NULL OR d.loaiDotThu = :loaiDotThu) AND " +
           "(:ngayBatDau IS NULL OR d.ngayBatDau >= :ngayBatDau) AND " +
           "(:ngayKetThuc IS NULL OR d.ngayKetThuc <= :ngayKetThuc)")
    Page<DotThu> search(@Param("tenDotThu") String tenDotThu,
                      @Param("loaiDotThu") String loaiDotThu,
                      @Param("ngayBatDau") LocalDate ngayBatDau,
                      @Param("ngayKetThuc") LocalDate ngayKetThuc,
                      Pageable pageable);
    
    List<DotThu> findByNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqual(LocalDate date1, LocalDate date2);
}

