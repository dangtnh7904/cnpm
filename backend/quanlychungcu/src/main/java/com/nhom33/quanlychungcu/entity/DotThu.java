package com.nhom33.quanlychungcu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DotThu")
public class DotThu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DotThu")
    private Integer id;

    @NotBlank(message = "Tên đợt thu không được để trống")
    @Size(max = 100, message = "Tên đợt thu không được vượt quá 100 ký tự")
    @Column(name = "TenDotThu", nullable = false, length = 100)
    private String tenDotThu;

    @Size(max = 50, message = "Loại đợt thu không được vượt quá 50 ký tự")
    @Column(name = "LoaiDotThu", length = 50)
    private String loaiDotThu; // 'PhiSinhHoat' hoặc 'DongGop'

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Column(name = "NgayKetThuc", nullable = false)
    private LocalDate ngayKetThuc;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    // Relationships
    @OneToMany(mappedBy = "dotThu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoaDon> danhSachHoaDon = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }

    // Constructors
    public DotThu() {
    }

    public DotThu(String tenDotThu, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.tenDotThu = tenDotThu;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenDotThu() {
        return tenDotThu;
    }

    public void setTenDotThu(String tenDotThu) {
        this.tenDotThu = tenDotThu;
    }

    public String getLoaiDotThu() {
        return loaiDotThu;
    }

    public void setLoaiDotThu(String loaiDotThu) {
        this.loaiDotThu = loaiDotThu;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public List<HoaDon> getDanhSachHoaDon() {
        return danhSachHoaDon;
    }

    public void setDanhSachHoaDon(List<HoaDon> danhSachHoaDon) {
        this.danhSachHoaDon = danhSachHoaDon;
    }
}

