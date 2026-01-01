package com.nhom33.quanlychungcu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "HoaDon")
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HoaDon")
    private Integer id;

    @NotNull(message = "Hộ gia đình không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_HoGiaDinh", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private HoGiaDinh hoGiaDinh;

    @NotNull(message = "Đợt thu không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DotThu", nullable = false)
    private DotThu dotThu;

    @NotNull(message = "Tổng tiền phải thu không được để trống")
    @DecimalMin(value = "0.0", message = "Tổng tiền phải thu phải >= 0")
    @Column(name = "TongTienPhaiThu", precision = 18, scale = 0)
    private BigDecimal tongTienPhaiThu = BigDecimal.ZERO;

    @NotNull(message = "Số tiền đã đóng không được để trống")
    @DecimalMin(value = "0.0", message = "Số tiền đã đóng phải >= 0")
    @Column(name = "SoTienDaDong", precision = 18, scale = 0)
    private BigDecimal soTienDaDong = BigDecimal.ZERO;

    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    @Column(name = "TrangThai", length = 50)
    private String trangThai = "Chưa đóng";

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    // Relationships - orphanRemoval=true để cascade delete hoạt động đúng
    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChiTietHoaDon> danhSachChiTiet = new ArrayList<>();

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LichSuThanhToan> danhSachThanhToan = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }

    // Constructors
    public HoaDon() {
    }

    public HoaDon(HoGiaDinh hoGiaDinh, DotThu dotThu) {
        this.hoGiaDinh = hoGiaDinh;
        this.dotThu = dotThu;
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

    public DotThu getDotThu() {
        return dotThu;
    }

    public void setDotThu(DotThu dotThu) {
        this.dotThu = dotThu;
    }

    public BigDecimal getTongTienPhaiThu() {
        return tongTienPhaiThu;
    }

    public void setTongTienPhaiThu(BigDecimal tongTienPhaiThu) {
        this.tongTienPhaiThu = tongTienPhaiThu;
    }

    public BigDecimal getSoTienDaDong() {
        return soTienDaDong;
    }

    public void setSoTienDaDong(BigDecimal soTienDaDong) {
        this.soTienDaDong = soTienDaDong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public List<ChiTietHoaDon> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietHoaDon> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet;
    }

    public List<LichSuThanhToan> getDanhSachThanhToan() {
        return danhSachThanhToan;
    }

    public void setDanhSachThanhToan(List<LichSuThanhToan> danhSachThanhToan) {
        this.danhSachThanhToan = danhSachThanhToan;
    }

    // Helper method to calculate remaining amount
    public BigDecimal getSoTienConNo() {
        return tongTienPhaiThu.subtract(soTienDaDong);
    }
}

