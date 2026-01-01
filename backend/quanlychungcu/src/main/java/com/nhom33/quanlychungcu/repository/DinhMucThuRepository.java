package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.DinhMucThu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DinhMucThuRepository extends JpaRepository<DinhMucThu, Integer> {
    
    List<DinhMucThu> findByHoGiaDinhId(Integer idHoGiaDinh);
    
    Page<DinhMucThu> findByHoGiaDinhId(Integer idHoGiaDinh, Pageable pageable);
    
    Optional<DinhMucThu> findByHoGiaDinhIdAndLoaiPhiId(Integer idHoGiaDinh, Integer idLoaiPhi);
    
    @Query("SELECT d FROM DinhMucThu d WHERE d.hoGiaDinh.id = :idHoGiaDinh AND d.loaiPhi.dangHoatDong = true")
    List<DinhMucThu> findActiveByHoGiaDinhId(@Param("idHoGiaDinh") Integer idHoGiaDinh);
}

