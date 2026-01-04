package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity liên kết User với Tòa nhà.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Manager gắn user (bằng username) vào tòa nhà mình quản lý
 * - User có thể thuộc nhiều tòa nhà
 * - Dùng để xác định user xem được thông báo của tòa nào
 */
@Entity
@Table(name = "UserToaNha", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ID_User", "ID_ToaNha"})
})
@Getter
@Setter
@NoArgsConstructor
public class UserToaNha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_User", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash"})
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ToaNha", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ToaNha toaNha;

    @Column(name = "NgayThem")
    private LocalDateTime ngayThem = LocalDateTime.now();

    public UserToaNha(UserAccount user, ToaNha toaNha) {
        this.user = user;
        this.toaNha = toaNha;
        this.ngayThem = LocalDateTime.now();
    }
}
