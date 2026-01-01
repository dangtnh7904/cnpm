package com.nhom33.quanlychungcu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietHoaDon")
public class ChiTietHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ChiTiet")
    private Integer id;

    @NotNull(message = "Hóa đơn không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_HoaDon", nullable = false)
    private HoaDon hoaDon;

    @NotNull(message = "Loại phí không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_LoaiPhi", nullable = false)
    private LoaiPhi loaiPhi;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    @Column(name = "SoLuong")
    private Double soLuong;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn giá phải >= 0")
    @Column(name = "DonGia", precision = 18, scale = 0)
    private BigDecimal donGia;

    @NotNull(message = "Thành tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Thành tiền phải >= 0")
    @Column(name = "ThanhTien", precision = 18, scale = 0)
    private BigDecimal thanhTien;

    // Constructors
    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(HoaDon hoaDon, LoaiPhi loaiPhi, Double soLuong, BigDecimal donGia) {
        this.hoaDon = hoaDon;
        this.loaiPhi = loaiPhi;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
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
        calculateThanhTien();
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
        calculateThanhTien();
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    private void calculateThanhTien() {
        if (soLuong != null && donGia != null) {
            this.thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
        }
    }
}

