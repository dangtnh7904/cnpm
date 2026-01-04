package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Phản ánh - User gửi phản ánh cho tòa nhà.
 * Manager của tòa nhà đó sẽ nhận và xử lý.
 */
@Entity
@Table(name = "PhanAnh")
public class PhanAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PhanAnh")
    private Integer id;

    @NotNull(message = "User không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_User", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private UserAccount user;

    @NotNull(message = "Tòa nhà không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ToaNha", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ToaNha toaNha;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    @Column(name = "TieuDe", nullable = false, length = 200)
    private String tieuDe;

    @NotBlank(message = "Nội dung không được để trống")
    @Column(name = "NoiDung", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "NgayGui")
    private LocalDateTime ngayGui;

    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    @Column(name = "TrangThai", length = 50)
    private String trangThai = "Chờ xử lý";

    // Relationships - orphanRemoval=true để cascade delete hoạt động đúng
    @OneToMany(mappedBy = "phanAnh", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore  // Ngăn circular reference - lấy phan hoi bằng API riêng
    private List<PhanHoi> danhSachPhanHoi = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (ngayGui == null) {
            ngayGui = LocalDateTime.now();
        }
    }

    // Constructors
    public PhanAnh() {
    }

    public PhanAnh(UserAccount user, ToaNha toaNha, String tieuDe, String noiDung) {
        this.user = user;
        this.toaNha = toaNha;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public ToaNha getToaNha() {
        return toaNha;
    }

    public void setToaNha(ToaNha toaNha) {
        this.toaNha = toaNha;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getNgayGui() {
        return ngayGui;
    }

    public void setNgayGui(LocalDateTime ngayGui) {
        this.ngayGui = ngayGui;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public List<PhanHoi> getDanhSachPhanHoi() {
        return danhSachPhanHoi;
    }

    public void setDanhSachPhanHoi(List<PhanHoi> danhSachPhanHoi) {
        this.danhSachPhanHoi = danhSachPhanHoi;
    }
}

