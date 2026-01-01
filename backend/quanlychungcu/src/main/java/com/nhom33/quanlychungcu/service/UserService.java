package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.Role;
import com.nhom33.quanlychungcu.entity.UserAccount;
import com.nhom33.quanlychungcu.repository.UserAccountRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAccount> getAllUsers() {
        return userRepo.findAll(Sort.by(Sort.Direction.ASC, "username"));
    }

    public Optional<UserAccount> getUserById(Integer id) {
        return userRepo.findById(id);
    }

    @Transactional
    public UserAccount updateUser(Integer id, String fullName, String email, Role role, String password) {
        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        
        return userRepo.save(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        if (!userRepo.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepo.deleteById(id);
    }
}
