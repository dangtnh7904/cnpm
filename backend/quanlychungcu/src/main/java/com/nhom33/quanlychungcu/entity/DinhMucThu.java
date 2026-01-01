package com.nhom33.quanlychungcu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Objects;

@Entity
@Table(name = "DinhMucThu")
public class DinhMucThu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DinhMuc")
    private Integer id;

    @NotNull(message = "Hộ gia đình không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_HoGiaDinh", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private HoGiaDinh hoGiaDinh;

    @NotNull(message = "Loại phí không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_LoaiPhi", nullable = false)
    private LoaiPhi loaiPhi;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    @Column(name = "SoLuong")
    private Double soLuong = 1.0;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    // Constructors
    public DinhMucThu() {
    }

    public DinhMucThu(HoGiaDinh hoGiaDinh, LoaiPhi loaiPhi, Double soLuong) {
        this.hoGiaDinh = hoGiaDinh;
        this.loaiPhi = loaiPhi;
        this.soLuong = soLuong;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public HoGiaDinh getHoGiaDinh() {
        return hoGiaDinh;
    }

    public void setHoGiaDinh(HoGiaDinh hoGiaDinh) {
        this.hoGiaDinh = hoGiaDinh;
    }

    public LoaiPhi getLoaiPhi() {
        return loaiPhi;
    }

    public void setLoaiPhi(LoaiPhi loaiPhi) {
        this.loaiPhi = loaiPhi;
    }

    public Double getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Double soLuong) {
        this.soLuong = soLuong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DinhMucThu that = (DinhMucThu) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

