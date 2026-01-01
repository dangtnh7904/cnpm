package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Integer> {
    
    List<ChiTietHoaDon> findByHoaDonId(Integer idHoaDon);
}

