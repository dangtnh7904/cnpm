package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    
    Page<ThongBao> findByLoaiThongBao(String loaiThongBao, Pageable pageable);
    
    @Query("SELECT t FROM ThongBao t WHERE " +
           "(:loaiThongBao IS NULL OR t.loaiThongBao = :loaiThongBao) AND " +
           "(:tieuDe IS NULL OR t.tieuDe LIKE %:tieuDe%)")
    Page<ThongBao> search(@Param("loaiThongBao") String loaiThongBao,
                         @Param("tieuDe") String tieuDe,
                         Pageable pageable);
}

