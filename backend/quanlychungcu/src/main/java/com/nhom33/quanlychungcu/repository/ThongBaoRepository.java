package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    
    Page<ThongBao> findByLoaiThongBao(String loaiThongBao, Pageable pageable);
    
    @Query("SELECT t FROM ThongBao t WHERE " +
           "(:loaiThongBao IS NULL OR t.loaiThongBao = :loaiThongBao) AND " +
           "(:tieuDe IS NULL OR t.tieuDe LIKE %:tieuDe%)")
    Page<ThongBao> search(@Param("loaiThongBao") String loaiThongBao,
                         @Param("tieuDe") String tieuDe,
                         Pageable pageable);
    
    // ===== Multi-tenancy v4.0: Mọi thông báo đều thuộc về một tòa nhà =====
    
    /**
     * Lấy thông báo của các tòa nhà chỉ định.
     */
    @Query("SELECT t FROM ThongBao t WHERE t.toaNha.id IN :toaNhaIds")
    Page<ThongBao> findByToaNhaIdIn(@Param("toaNhaIds") List<Integer> toaNhaIds, Pageable pageable);
    
    /**
     * Tìm kiếm thông báo với multi-tenancy.
     */
    @Query("SELECT t FROM ThongBao t WHERE " +
           "t.toaNha.id IN :toaNhaIds AND " +
           "(:loaiThongBao IS NULL OR t.loaiThongBao = :loaiThongBao) AND " +
           "(:tieuDe IS NULL OR t.tieuDe LIKE %:tieuDe%)")
    Page<ThongBao> searchByToaNhaIds(@Param("toaNhaIds") List<Integer> toaNhaIds,
                                     @Param("loaiThongBao") String loaiThongBao,
                                     @Param("tieuDe") String tieuDe,
                                     Pageable pageable);
}

