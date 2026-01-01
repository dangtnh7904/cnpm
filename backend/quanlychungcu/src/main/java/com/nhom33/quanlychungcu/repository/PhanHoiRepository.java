package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.PhanHoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhanHoiRepository extends JpaRepository<PhanHoi, Integer> {
    
    List<PhanHoi> findByPhanAnhId(Integer idPhanAnh);
}

