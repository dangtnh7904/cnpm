package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity: Bảng giá dịch vụ theo tòa nhà.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Cho phép set giá riêng cho từng loại phí tại từng tòa nhà.
 * - Ưu tiên giá: BangGiaDichVu > LoaiPhi.DonGia (Base Price).
 * - Mỗi cặp (ID_LoaiPhi, ID_ToaNha) chỉ có 1 bản ghi (UNIQUE constraint).
 */
@Entity
@Table(name = "BangGiaDichVu", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ID_LoaiPhi", "ID_ToaNha"}, name = "UC_BangGia_Unique")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BangGiaDichVu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_BangGia")
    private Integer id;

    @NotNull(message = "Loại phí không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_LoaiPhi", nullable = false)
    @JsonIgnoreProperties({"bangGiaDichVuList"})
    private LoaiPhi loaiPhi;

    @NotNull(message = "Tòa nhà không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ToaNha", nullable = false)
    @JsonIgnoreProperties({"danhSachHoGiaDinh"})
    private ToaNha toaNha;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0", message = "Đơn giá không được âm")
    @Column(name = "DonGia", nullable = false, precision = 18, scale = 0)
    private BigDecimal donGia;

    @Column(name = "NgayApDung")
    private LocalDateTime ngayApDung;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    // ===== Constructors =====

    public BangGiaDichVu() {
        this.ngayApDung = LocalDateTime.now();
    }

    public BangGiaDichVu(LoaiPhi loaiPhi, ToaNha toaNha, BigDecimal donGia) {
        this.loaiPhi = loaiPhi;
        this.toaNha = toaNha;
        this.donGia = donGia;
        this.ngayApDung = LocalDateTime.now();
    }

    // ===== Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        if (ngayApDung == null) {
            ngayApDung = LocalDateTime.now();
        }
    }

    // ===== Getters & Setters =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LoaiPhi getLoaiPhi() {
        return loaiPhi;
    }

    public void setLoaiPhi(LoaiPhi loaiPhi) {
        this.loaiPhi = loaiPhi;
    }

    public ToaNha getToaNha() {
        return toaNha;
    }

    public void setToaNha(ToaNha toaNha) {
        this.toaNha = toaNha;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public LocalDateTime getNgayApDung() {
        return ngayApDung;
    }

    public void setNgayApDung(LocalDateTime ngayApDung) {
        this.ngayApDung = ngayApDung;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    // ===== Helper Methods =====

    @Override
    public String toString() {
        return "BangGiaDichVu{" +
                "id=" + id +
                ", loaiPhi=" + (loaiPhi != null ? loaiPhi.getTenLoaiPhi() : "null") +
                ", toaNha=" + (toaNha != null ? toaNha.getTenToaNha() : "null") +
                ", donGia=" + donGia +
                '}';
    }
}
