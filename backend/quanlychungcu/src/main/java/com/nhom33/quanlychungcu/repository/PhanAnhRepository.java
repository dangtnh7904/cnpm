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
    
    // Lấy phản ánh theo user
    List<PhanAnh> findByUserId(Integer userId);
    Page<PhanAnh> findByUserId(Integer userId, Pageable pageable);
    
    // Lấy phản ánh theo tòa nhà (Manager xem)
    List<PhanAnh> findByToaNhaId(Integer toaNhaId);
    Page<PhanAnh> findByToaNhaId(Integer toaNhaId, Pageable pageable);
    
    // Lấy phản ánh theo tòa nhà (nhiều tòa)
    Page<PhanAnh> findByToaNhaIdIn(List<Integer> toaNhaIds, Pageable pageable);
    
    Page<PhanAnh> findByTrangThai(String trangThai, Pageable pageable);
    
    @Query("SELECT p FROM PhanAnh p WHERE " +
           "(:userId IS NULL OR p.user.id = :userId) AND " +
           "(:toaNhaId IS NULL OR p.toaNha.id = :toaNhaId) AND " +
           "(:trangThai IS NULL OR p.trangThai = :trangThai) AND " +
           "(:tieuDe IS NULL OR p.tieuDe LIKE %:tieuDe%)")
    Page<PhanAnh> search(@Param("userId") Integer userId,
                        @Param("toaNhaId") Integer toaNhaId,
                        @Param("trangThai") String trangThai,
                        @Param("tieuDe") String tieuDe,
                        Pageable pageable);
    
    // Tìm kiếm với multi-tenancy (Manager chỉ xem tòa mình)
    @Query("SELECT p FROM PhanAnh p WHERE " +
           "p.toaNha.id IN :toaNhaIds AND " +
           "(:trangThai IS NULL OR p.trangThai = :trangThai) AND " +
           "(:tieuDe IS NULL OR p.tieuDe LIKE %:tieuDe%)")
    Page<PhanAnh> searchByToaNhaIds(@Param("toaNhaIds") List<Integer> toaNhaIds,
                                    @Param("trangThai") String trangThai,
                                    @Param("tieuDe") String tieuDe,
                                    Pageable pageable);
}

