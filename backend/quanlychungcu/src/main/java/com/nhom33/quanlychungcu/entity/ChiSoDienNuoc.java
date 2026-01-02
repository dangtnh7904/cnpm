package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity: Chỉ số Điện Nước.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Lưu chỉ số cũ/mới của từng hộ gia đình trong mỗi đợt thu
 * - ChiSoMoi = NULL nghĩa là "Chưa nhập chỉ số"
 * - ChiSoCu được tự động lấy từ ChiSoMoi của tháng trước
 * - Unique constraint: Mỗi hộ chỉ có 1 bản ghi/đợt/loại phí
 */
@Entity
@Table(name = "ChiSoDienNuoc", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ID_HoGiaDinh", "ID_DotThu", "ID_LoaiPhi"})
})
public class ChiSoDienNuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ChiSo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_HoGiaDinh", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "danhSachNhanKhau", "danhSachHoaDon", "danhSachDinhMuc", "danhSachPhanAnh"})
    private HoGiaDinh hoGiaDinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DotThu", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "danhSachHoaDon"})
    private DotThu dotThu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_LoaiPhi", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "danhSachDinhMuc", "danhSachChiTiet"})
    private LoaiPhi loaiPhi;

    @Column(name = "ChiSoCu")
    private Integer chiSoCu = 0;

    @Column(name = "ChiSoMoi")
    private Integer chiSoMoi; // NULL = Chưa nhập

    @Column(name = "NgayChot")
    private LocalDateTime ngayChot;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (chiSoMoi != null) {
            ngayChot = LocalDateTime.now();
        }
    }

    // Constructors
    public ChiSoDienNuoc() {
    }

    public ChiSoDienNuoc(HoGiaDinh hoGiaDinh, DotThu dotThu, LoaiPhi loaiPhi) {
        this.hoGiaDinh = hoGiaDinh;
        this.dotThu = dotThu;
        this.loaiPhi = loaiPhi;
        this.chiSoCu = 0;
    }

    // Business methods
    public Integer getTieuThu() {
        if (chiSoMoi == null) return null;
        return Math.max(0, chiSoMoi - (chiSoCu != null ? chiSoCu : 0));
    }

    public boolean isDaNhap() {
        return chiSoMoi != null;
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

    public LoaiPhi getLoaiPhi() {
        return loaiPhi;
    }

    public void setLoaiPhi(LoaiPhi loaiPhi) {
        this.loaiPhi = loaiPhi;
    }

    public Integer getChiSoCu() {
        return chiSoCu;
    }

    public void setChiSoCu(Integer chiSoCu) {
        this.chiSoCu = chiSoCu;
    }

    public Integer getChiSoMoi() {
        return chiSoMoi;
    }

    public void setChiSoMoi(Integer chiSoMoi) {
        this.chiSoMoi = chiSoMoi;
    }

    public LocalDateTime getNgayChot() {
        return ngayChot;
    }

    public void setNgayChot(LocalDateTime ngayChot) {
        this.ngayChot = ngayChot;
    }
}
