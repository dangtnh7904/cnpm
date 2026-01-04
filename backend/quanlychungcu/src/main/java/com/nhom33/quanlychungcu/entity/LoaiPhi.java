package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Loại Phí.
 * 
 * MULTI-TENANCY v4.1 - PHÍ CHUNG CỦA MANAGER:
 * - Mỗi Manager có danh sách loại phí CHUNG của riêng mình (toaNha = NULL)
 * - Loại phí chung không thuộc tòa nhà cụ thể nào
 * - Giá riêng cho từng tòa nhà được cấu hình qua BangGiaDichVu
 * - Ưu tiên giá: BangGiaDichVu > LoaiPhi.DonGia (giá mặc định)
 */
@Entity
@Table(name = "LoaiPhi")
public class LoaiPhi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LoaiPhi")
    private Integer id;

    @NotBlank(message = "Tên loại phí không được để trống")
    @Size(max = 100, message = "Tên loại phí không được vượt quá 100 ký tự")
    @Column(name = "TenLoaiPhi", nullable = false, length = 100)
    private String tenLoaiPhi;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn giá phải >= 0")
    @Column(name = "DonGia", precision = 18, scale = 0)
    private BigDecimal donGia;

    @Size(max = 50, message = "Đơn vị tính không được vượt quá 50 ký tự")
    @Column(name = "DonViTinh", length = 50)
    private String donViTinh;

    @NotBlank(message = "Loại thu không được để trống")
    @Size(max = 50, message = "Loại thu không được vượt quá 50 ký tự")
    @Column(name = "LoaiThu", nullable = false, length = 50)
    private String loaiThu; // 'BatBuoc' hoặc 'TuNguyen'

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    @Column(name = "MoTa", length = 255)
    private String moTa;

    @Column(name = "DangHoatDong")
    private Boolean dangHoatDong = true;

    // Multi-tenancy v4.1: Loại phí thuộc về MANAGER, không thuộc tòa nhà cụ thể
    // nguoiQuanLy = Manager sở hữu loại phí này
    // KHÔNG dùng @NotNull ở đây vì service sẽ tự động gán từ user đang đăng nhập
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_NguoiQuanLy", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private UserAccount nguoiQuanLy;

    // toaNha = NULL nghĩa là phí CHUNG của Manager (áp dụng cho tất cả tòa)
    // toaNha != NULL nghĩa là phí RIÊNG chỉ áp dụng cho tòa đó
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ToaNha", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "danhSachHoGiaDinh", "nguoiQuanLy"})
    private ToaNha toaNha;

    // Relationships
    @OneToMany(mappedBy = "loaiPhi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DinhMucThu> danhSachDinhMuc = new ArrayList<>();

    @OneToMany(mappedBy = "loaiPhi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChiTietHoaDon> danhSachChiTiet = new ArrayList<>();

    // Constructors
    public LoaiPhi() {
    }

    public LoaiPhi(String tenLoaiPhi, BigDecimal donGia, String loaiThu) {
        this.tenLoaiPhi = tenLoaiPhi;
        this.donGia = donGia;
        this.loaiThu = loaiThu;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenLoaiPhi() {
        return tenLoaiPhi;
    }

    public void setTenLoaiPhi(String tenLoaiPhi) {
        this.tenLoaiPhi = tenLoaiPhi;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getLoaiThu() {
        return loaiThu;
    }

    public void setLoaiThu(String loaiThu) {
        this.loaiThu = loaiThu;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Boolean getDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(Boolean dangHoatDong) {
        this.dangHoatDong = dangHoatDong;
    }

    public List<DinhMucThu> getDanhSachDinhMuc() {
        return danhSachDinhMuc;
    }

    public void setDanhSachDinhMuc(List<DinhMucThu> danhSachDinhMuc) {
        this.danhSachDinhMuc = danhSachDinhMuc;
    }

    public List<ChiTietHoaDon> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietHoaDon> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet;
    }

    public ToaNha getToaNha() {
        return toaNha;
    }

    public void setToaNha(ToaNha toaNha) {
        this.toaNha = toaNha;
    }

    public UserAccount getNguoiQuanLy() {
        return nguoiQuanLy;
    }

    public void setNguoiQuanLy(UserAccount nguoiQuanLy) {
        this.nguoiQuanLy = nguoiQuanLy;
    }

    /**
     * Kiểm tra đây có phải là phí chung (không gắn với tòa cụ thể) không.
     */
    public boolean isPhiChung() {
        return toaNha == null;
    }
}

