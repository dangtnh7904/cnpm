package com.nhom33.quanlychungcu.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO: Chi tiết giá của một loại phí.
 * 
 * DÙNG CHO:
 * - Cấu hình giá riêng của một loại phí tại một tòa nhà.
 * - Là phần tử trong danh sách CauHinhGiaDTO.danhSachGia
 */
public class ChiTietGiaDTO {

    @NotNull(message = "ID loại phí không được để trống")
    private Integer loaiPhiId;

    private String tenLoaiPhi; // Optional: Để hiển thị

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0", message = "Đơn giá không được âm")
    private BigDecimal donGiaRieng;

    private String ghiChu;

    // ===== Constructors =====

    public ChiTietGiaDTO() {
    }

    public ChiTietGiaDTO(Integer loaiPhiId, BigDecimal donGiaRieng) {
        this.loaiPhiId = loaiPhiId;
        this.donGiaRieng = donGiaRieng;
    }

    public ChiTietGiaDTO(Integer loaiPhiId, String tenLoaiPhi, BigDecimal donGiaRieng) {
        this.loaiPhiId = loaiPhiId;
        this.tenLoaiPhi = tenLoaiPhi;
        this.donGiaRieng = donGiaRieng;
    }

    // ===== Getters & Setters =====

    public Integer getLoaiPhiId() {
        return loaiPhiId;
    }

    public void setLoaiPhiId(Integer loaiPhiId) {
        this.loaiPhiId = loaiPhiId;
    }

    public String getTenLoaiPhi() {
        return tenLoaiPhi;
    }

    public void setTenLoaiPhi(String tenLoaiPhi) {
        this.tenLoaiPhi = tenLoaiPhi;
    }

    public BigDecimal getDonGiaRieng() {
        return donGiaRieng;
    }

    public void setDonGiaRieng(BigDecimal donGiaRieng) {
        this.donGiaRieng = donGiaRieng;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
