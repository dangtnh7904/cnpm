package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
}

