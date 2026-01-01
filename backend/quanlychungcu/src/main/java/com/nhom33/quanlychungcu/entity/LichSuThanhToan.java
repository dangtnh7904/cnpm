package com.nhom33.quanlychungcu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LichSuThanhToan")
public class LichSuThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_GiaoDich")
    private Integer id;

    @NotNull(message = "Hóa đơn không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_HoaDon", nullable = false)
    private HoaDon hoaDon;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền phải > 0")
    @Column(name = "SoTien", nullable = false, precision = 18, scale = 0)
    private BigDecimal soTien;

    @Column(name = "NgayNop")
    private LocalDateTime ngayNop;

    @Size(max = 50, message = "Hình thức không được vượt quá 50 ký tự")
    @Column(name = "HinhThuc", length = 50)
    private String hinhThuc; // 'Tiền mặt' / 'Chuyển khoản' / 'VNPay' / 'Momo'

    @Size(max = 100, message = "Người nộp không được vượt quá 100 ký tự")
    @Column(name = "NguoiNop", length = 100)
    private String nguoiNop;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    @PrePersist
    protected void onCreate() {
        if (ngayNop == null) {
            ngayNop = LocalDateTime.now();
        }
    }

    // Constructors
    public LichSuThanhToan() {
    }

    public LichSuThanhToan(HoaDon hoaDon, BigDecimal soTien, String hinhThuc) {
        this.hoaDon = hoaDon;
        this.soTien = soTien;
        this.hinhThuc = hinhThuc;
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

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public LocalDateTime getNgayNop() {
        return ngayNop;
    }

    public void setNgayNop(LocalDateTime ngayNop) {
        this.ngayNop = ngayNop;
    }

    public String getHinhThuc() {
        return hinhThuc;
    }

    public void setHinhThuc(String hinhThuc) {
        this.hinhThuc = hinhThuc;
    }

    public String getNguoiNop() {
        return nguoiNop;
    }

    public void setNguoiNop(String nguoiNop) {
        this.nguoiNop = nguoiNop;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

