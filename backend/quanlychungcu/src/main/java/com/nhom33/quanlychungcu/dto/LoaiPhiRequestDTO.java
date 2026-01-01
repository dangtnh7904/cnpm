package com.nhom33.quanlychungcu.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO: Request tạo/cập nhật Loại Phí.
 * 
 * LOGIC:
 * - DonGia ở đây là giá mặc định (Base Price).
 * - Giá riêng theo tòa nhà sẽ được cấu hình qua BangGiaDichVu.
 */
public class LoaiPhiRequestDTO {

    private Integer id;

    @NotBlank(message = "Tên loại phí không được để trống")
    @Size(max = 100, message = "Tên loại phí không được vượt quá 100 ký tự")
    private String tenLoaiPhi;

    @NotNull(message = "Đơn giá mặc định không được để trống")
    @DecimalMin(value = "0", message = "Đơn giá không được âm")
    private BigDecimal donGia;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 50, message = "Đơn vị tính không được vượt quá 50 ký tự")
    private String donViTinh;

    @NotBlank(message = "Loại phí (bắt buộc/không bắt buộc) không được để trống")
    @Pattern(regexp = "Bắt buộc|Không bắt buộc", message = "Loại phí phải là 'Bắt buộc' hoặc 'Không bắt buộc'")
    private String loai;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String moTa;

    // ===== Constructors =====

    public LoaiPhiRequestDTO() {
    }

    public LoaiPhiRequestDTO(String tenLoaiPhi, BigDecimal donGia, String donViTinh, String loai) {
        this.tenLoaiPhi = tenLoaiPhi;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.loai = loai;
    }

    // ===== Getters & Setters =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenLoaiPhi() {
        return tenLoaiPhi;
    }

    public void setTenLoaiPhi(String tenLoaiPhi) {
        this.tenLoaiPhi = tenLoaiPhi;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
