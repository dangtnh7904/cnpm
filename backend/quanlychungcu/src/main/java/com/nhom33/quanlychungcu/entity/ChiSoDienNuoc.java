package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity: Chỉ số Điện Nước.
 * 
 * LOGIC NGHIỆP VỤ MỚI (Tách rời ghi số và thu tiền):
 * - Ghi chỉ số là hoạt động cố định hàng tháng (chốt ngày 24)
 * - Không phụ thuộc vào Đợt thu
 * - Lưu theo Tháng/Năm và Hộ gia đình
 * - Unique constraint: Mỗi hộ, mỗi loại phí, mỗi tháng chỉ có 1 bản ghi
 * - Khi tạo Đợt thu có phí Điện/Nước: Query bảng này để tính tiền
 */
@Entity
@Table(name = "ChiSoDienNuoc", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ID_HoGiaDinh", "ID_LoaiPhi", "Thang", "Nam"})
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_LoaiPhi", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "danhSachDinhMuc", "danhSachChiTiet"})
    private LoaiPhi loaiPhi;

    @Column(name = "Thang", nullable = false)
    private Integer thang; // 1-12

    @Column(name = "Nam", nullable = false)
    private Integer nam; // Năm ghi sổ

    @Column(name = "ChiSoMoi", nullable = false)
    private Integer chiSoMoi; // Chỉ số chốt ngày 24

    @Column(name = "NgayChot")
    private LocalDateTime ngayChot;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        ngayChot = LocalDateTime.now();
    }

    // Constructors
    public ChiSoDienNuoc() {
    }

    public ChiSoDienNuoc(HoGiaDinh hoGiaDinh, LoaiPhi loaiPhi, Integer thang, Integer nam, Integer chiSoMoi) {
        this.hoGiaDinh = hoGiaDinh;
        this.loaiPhi = loaiPhi;
        this.thang = thang;
        this.nam = nam;
        this.chiSoMoi = chiSoMoi;
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

    public Integer getThang() {
        return thang;
    }

    public void setThang(Integer thang) {
        this.thang = thang;
    }

    public Integer getNam() {
        return nam;
    }

    public void setNam(Integer nam) {
        this.nam = nam;
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
