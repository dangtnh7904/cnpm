package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Entity: Cấu hình Loại phí trong Đợt thu.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Mỗi Đợt thu có thể có nhiều Loại phí (Điện, Nước, Quản lý, ...)
 * - Khi tạo Đợt thu mới, tự động thêm các phí bắt buộc (Điện, Nước)
 * - Không cho xóa phí Điện, Nước khỏi đợt thu
 */
@Entity
@Table(name = "DotThu_LoaiPhi", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ID_DotThu", "ID_LoaiPhi"})
})
public class DotThuLoaiPhi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Config")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DotThu", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "danhSachHoaDon"})
    private DotThu dotThu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_LoaiPhi", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "danhSachDinhMuc", "danhSachChiTiet"})
    private LoaiPhi loaiPhi;

    // Constructors
    public DotThuLoaiPhi() {
    }

    public DotThuLoaiPhi(DotThu dotThu, LoaiPhi loaiPhi) {
        this.dotThu = dotThu;
        this.loaiPhi = loaiPhi;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
}
