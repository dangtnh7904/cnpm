package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.PhanAnh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhanAnhRepository extends JpaRepository<PhanAnh, Integer> {
    
    List<PhanAnh> findByHoGiaDinhId(Integer idHoGiaDinh);
    
    Page<PhanAnh> findByHoGiaDinhId(Integer idHoGiaDinh, Pageable pageable);
    
    Page<PhanAnh> findByTrangThai(String trangThai, Pageable pageable);
    
    @Query("SELECT p FROM PhanAnh p WHERE " +
           "(:idHoGiaDinh IS NULL OR p.hoGiaDinh.id = :idHoGiaDinh) AND " +
           "(:trangThai IS NULL OR p.trangThai = :trangThai) AND " +
           "(:tieuDe IS NULL OR p.tieuDe LIKE %:tieuDe%)")
    Page<PhanAnh> search(@Param("idHoGiaDinh") Integer idHoGiaDinh,
                        @Param("trangThai") String trangThai,
                        @Param("tieuDe") String tieuDe,
                        Pageable pageable);
}

