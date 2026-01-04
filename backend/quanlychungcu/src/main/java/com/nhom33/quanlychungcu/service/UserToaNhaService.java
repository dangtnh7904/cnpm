package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.entity.UserAccount;
import com.nhom33.quanlychungcu.entity.UserToaNha;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;
import com.nhom33.quanlychungcu.repository.UserAccountRepository;
import com.nhom33.quanlychungcu.repository.UserToaNhaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service quản lý liên kết User với Tòa nhà.
 * 
 * LOGIC NGHIỆP VỤ:
 * - ADMIN: Có thể gắn bất kỳ user vào bất kỳ tòa nhà
 * - MANAGER: Chỉ gắn user vào tòa nhà mình quản lý
 * - User thuộc tòa nhà nào thì xem được thông báo của tòa đó
 */
@Service
public class UserToaNhaService {

    private final UserToaNhaRepository repo;
    private final UserAccountRepository userRepo;
    private final ToaNhaRepository toaNhaRepo;
    private final SecurityHelper securityHelper;

    public UserToaNhaService(UserToaNhaRepository repo,
                             UserAccountRepository userRepo,
                             ToaNhaRepository toaNhaRepo,
                             SecurityHelper securityHelper) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.toaNhaRepo = toaNhaRepo;
        this.securityHelper = securityHelper;
    }

    /**
     * Gắn user vào tòa nhà (bằng username).
     * Manager chỉ được gắn user vào tòa nhà mình quản lý.
     */
    @Transactional
    public UserToaNha addUserToBuilding(String username, Integer toaNhaId) {
        // Kiểm tra quyền quản lý tòa nhà
        if (!securityHelper.canManageBuilding(toaNhaId)) {
            throw new AccessDeniedException("Bạn không có quyền quản lý tòa nhà này");
        }
        
        // Tìm user theo username
        UserAccount user = userRepo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với username: " + username));
        
        // Tìm tòa nhà
        ToaNha toaNha = toaNhaRepo.findById(toaNhaId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + toaNhaId));
        
        // Kiểm tra đã tồn tại chưa
        if (repo.existsByUserIdAndToaNhaId(user.getId(), toaNhaId)) {
            throw new IllegalArgumentException("User đã được gắn vào tòa nhà này");
        }
        
        UserToaNha link = new UserToaNha(user, toaNha);
        return repo.save(link);
    }

    /**
     * Xóa user khỏi tòa nhà.
     */
    @Transactional
    public void removeUserFromBuilding(Integer userId, Integer toaNhaId) {
        // Kiểm tra quyền quản lý tòa nhà
        if (!securityHelper.canManageBuilding(toaNhaId)) {
            throw new AccessDeniedException("Bạn không có quyền quản lý tòa nhà này");
        }
        
        if (!repo.existsByUserIdAndToaNhaId(userId, toaNhaId)) {
            throw new ResourceNotFoundException("User không thuộc tòa nhà này");
        }
        
        repo.deleteByUserIdAndToaNhaId(userId, toaNhaId);
    }

    /**
     * Lấy danh sách user trong tòa nhà.
     */
    public List<UserToaNha> getUsersInBuilding(Integer toaNhaId) {
        // Kiểm tra quyền xem tòa nhà
        if (!securityHelper.canAccessBuilding(toaNhaId)) {
            throw new AccessDeniedException("Bạn không có quyền xem tòa nhà này");
        }
        
        return repo.findByToaNhaId(toaNhaId);
    }

    /**
     * Lấy danh sách tòa nhà mà user được gắn vào.
     */
    public List<UserToaNha> getBuildingsOfUser(Integer userId) {
        return repo.findByUserId(userId);
    }

    /**
     * Lấy danh sách ID tòa nhà mà user được gắn vào.
     * Dùng cho SecurityHelper.
     */
    public List<Integer> getToaNhaIdsByUserId(Integer userId) {
        return repo.findToaNhaIdsByUserId(userId);
    }

    /**
     * Tìm user trong tòa nhà theo username.
     */
    public List<UserToaNha> searchUsersInBuilding(Integer toaNhaId, String username) {
        if (!securityHelper.canAccessBuilding(toaNhaId)) {
            throw new AccessDeniedException("Bạn không có quyền xem tòa nhà này");
        }
        
        return repo.findByToaNhaIdAndUsernameContaining(toaNhaId, username);
    }

    /**
     * Kiểm tra user có thuộc tòa nhà không.
     */
    public boolean isUserInBuilding(Integer userId, Integer toaNhaId) {
        return repo.existsByUserIdAndToaNhaId(userId, toaNhaId);
    }

    /**
     * User tự thoát khỏi tòa nhà.
     * Không cần kiểm tra quyền vì user tự rời khỏi.
     */
    @Transactional
    public void leaveBuilding(Integer userId, Integer toaNhaId) {
        if (!repo.existsByUserIdAndToaNhaId(userId, toaNhaId)) {
            throw new ResourceNotFoundException("Bạn không thuộc tòa nhà này");
        }
        
        repo.deleteByUserIdAndToaNhaId(userId, toaNhaId);
    }
}
