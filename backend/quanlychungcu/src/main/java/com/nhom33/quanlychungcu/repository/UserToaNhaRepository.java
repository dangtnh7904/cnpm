package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.UserToaNha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserToaNhaRepository extends JpaRepository<UserToaNha, Integer> {
    
    /**
     * Lấy danh sách tòa nhà mà user được gắn vào.
     */
    List<UserToaNha> findByUserId(Integer userId);
    
    /**
     * Lấy danh sách user trong một tòa nhà.
     */
    List<UserToaNha> findByToaNhaId(Integer toaNhaId);
    
    /**
     * Kiểm tra user đã được gắn vào tòa nhà chưa.
     */
    boolean existsByUserIdAndToaNhaId(Integer userId, Integer toaNhaId);
    
    /**
     * Tìm liên kết giữa user và tòa nhà.
     */
    Optional<UserToaNha> findByUserIdAndToaNhaId(Integer userId, Integer toaNhaId);
    
    /**
     * Xóa liên kết user khỏi tòa nhà.
     */
    void deleteByUserIdAndToaNhaId(Integer userId, Integer toaNhaId);
    
    /**
     * Lấy danh sách ID tòa nhà mà user được gắn vào.
     */
    @Query("SELECT ut.toaNha.id FROM UserToaNha ut WHERE ut.user.id = :userId")
    List<Integer> findToaNhaIdsByUserId(@Param("userId") Integer userId);
    
    /**
     * Lấy danh sách user theo username trong một tòa nhà.
     */
    @Query("SELECT ut FROM UserToaNha ut WHERE ut.toaNha.id = :toaNhaId AND ut.user.username LIKE %:username%")
    List<UserToaNha> findByToaNhaIdAndUsernameContaining(@Param("toaNhaId") Integer toaNhaId, 
                                                          @Param("username") String username);
}
