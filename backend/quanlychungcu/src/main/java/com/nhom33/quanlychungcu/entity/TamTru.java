package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity quản lý thông tin Giấy Tạm Trú.
 * 
 * LOGIC NGHIỆP VỤ:
 * - TamTru liên kết với NhanKhau (người tạm trú phải xuất hiện trong danh sách nhân khẩu).
 * - Khi đăng ký tạm trú, bắt buộc Insert NhanKhau trước với TrangThai = "Tạm trú".
 * - Entity này lưu thông tin giấy tờ tạm trú (ngày bắt đầu, ngày kết thúc, lý do...).
 */
@Entity
@Table(name = "TamTru")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TamTru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TamTru")
    private Integer id;

    /**
     * Liên kết với NhanKhau - người tạm trú.
     * Bắt buộc: Người tạm trú phải được insert vào bảng NhanKhau trước.
     */
    @NotNull(message = "Nhân khẩu tạm trú không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_NhanKhau", nullable = false)
    @JsonIgnoreProperties({"danhSachTamVang", "hoGiaDinh"})
    private NhanKhau nhanKhau;

    /**
     * Mã giấy tạm trú (nếu có - do cơ quan cấp).
     */
    @Size(max = 50, message = "Mã giấy tạm trú không được vượt quá 50 ký tự")
    @Column(name = "MaGiayTamTru", length = 50)
    private String maGiayTamTru;

    /**
     * Địa chỉ thường trú (nơi đăng ký hộ khẩu gốc của người tạm trú).
     */
    @Size(max = 200, message = "Địa chỉ thường trú không được vượt quá 200 ký tự")
    @Column(name = "DiaChiThuongTru", length = 200)
    private String diaChiThuongTru;

    @NotNull(message = "Ngày bắt đầu tạm trú không được để trống")
    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;

    /**
     * Ngày hết hạn tạm trú (có thể null nếu chưa xác định).
     */
    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Size(max = 500, message = "Lý do tạm trú không được vượt quá 500 ký tự")
    @Column(name = "LyDo", length = 500)
    private String lyDo;

    @Column(name = "NgayDangKy")
    private LocalDateTime ngayDangKy;

    // ===== Lifecycle Callbacks =====
    
    @PrePersist
    protected void onCreate() {
        if (ngayDangKy == null) {
            ngayDangKy = LocalDateTime.now();
        }
    }

    // ===== Constructors =====
    
    public TamTru() {
    }
    
    public TamTru(NhanKhau nhanKhau, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.nhanKhau = nhanKhau;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    // ===== Getters & Setters =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public NhanKhau getNhanKhau() {
        return nhanKhau;
    }

    public void setNhanKhau(NhanKhau nhanKhau) {
        this.nhanKhau = nhanKhau;
    }

    public String getMaGiayTamTru() {
        return maGiayTamTru;
    }

    public void setMaGiayTamTru(String maGiayTamTru) {
        this.maGiayTamTru = maGiayTamTru;
    }

    public String getDiaChiThuongTru() {
        return diaChiThuongTru;
    }

    public void setDiaChiThuongTru(String diaChiThuongTru) {
        this.diaChiThuongTru = diaChiThuongTru;
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

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public LocalDateTime getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDateTime ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    // ===== Utility Methods =====
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TamTru tamTru = (TamTru) o;
        return Objects.equals(id, tamTru.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TamTru{" +
                "id=" + id +
                ", nhanKhauId=" + (nhanKhau != null ? nhanKhau.getId() : null) +
                ", ngayBatDau=" + ngayBatDau +
                ", ngayKetThuc=" + ngayKetThuc +
                ", ngayDangKy=" + ngayDangKy +
                '}';
    }
}
