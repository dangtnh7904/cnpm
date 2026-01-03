package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore // Prevent infinite recursion: LichSuThanhToan -> HoaDon -> LichSuThanhToan
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

    // === VNPAY Fields ===
    @Size(max = 50, message = "Mã giao dịch VNPAY không được vượt quá 50 ký tự")
    @Column(name = "MaGiaoDichVNPAY", length = 50)
    private String maGiaoDichVnpay;

    @Size(max = 20, message = "Mã ngân hàng không được vượt quá 20 ký tự")
    @Column(name = "MaNganHang", length = 20)
    private String maNganHang;

    @Size(max = 10, message = "Mã phản hồi không được vượt quá 10 ký tự")
    @Column(name = "MaPhanHoi", length = 10)
    private String maPhanHoi;

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

    // === VNPAY Getters/Setters ===
    public String getMaGiaoDichVnpay() {
        return maGiaoDichVnpay;
    }

    public void setMaGiaoDichVnpay(String maGiaoDichVnpay) {
        this.maGiaoDichVnpay = maGiaoDichVnpay;
    }

    public String getMaNganHang() {
        return maNganHang;
    }

    public void setMaNganHang(String maNganHang) {
        this.maNganHang = maNganHang;
    }

    public String getMaPhanHoi() {
        return maPhanHoi;
    }

    public void setMaPhanHoi(String maPhanHoi) {
        this.maPhanHoi = maPhanHoi;
    }
}

