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
    private final EmailService emailService;

    public UserService(UserAccountRepository userRepo, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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

    /**
     * Reset mật khẩu user về mật khẩu ngẫu nhiên và gửi email thông báo.
     */
    @Transactional
    public void resetPassword(Integer id) {
        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // Sinh mật khẩu ngẫu nhiên 8 ký tự
        String newPassword = generateRandomPassword(8);

        // Encode và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // Gửi email thông báo
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), newPassword);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
