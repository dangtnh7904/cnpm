package com.nhom33.quanlychungcu.repository;

import com.nhom33.quanlychungcu.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsername(String username);
}
