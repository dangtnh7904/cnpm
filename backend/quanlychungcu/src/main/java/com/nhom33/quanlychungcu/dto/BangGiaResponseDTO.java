package com.nhom33.quanlychungcu.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO: Response chi tiết bảng giá dịch vụ.
 * 
 * DÙNG CHO:
 * - Trả về thông tin bảng giá với đầy đủ chi tiết.
 * - Hiển thị trên frontend.
 */
public class BangGiaResponseDTO {

    private Integer id;

    // Loại phí
    private Integer loaiPhiId;
    private String tenLoaiPhi;
    private String donViTinh;
    private BigDecimal donGiaMacDinh; // Giá mặc định từ LoaiPhi

    // Tòa nhà
    private Integer toaNhaId;
    private String tenToaNha;

    // Giá riêng
    private BigDecimal donGiaRieng;
    private LocalDateTime ngayApDung;
    private String ghiChu;

    // Computed
    private BigDecimal donGiaApDung; // Giá thực tế áp dụng (ưu tiên giá riêng)

    // ===== Constructors =====

    public BangGiaResponseDTO() {
    }

    // ===== Factory Methods =====

    /**
     * Tạo DTO từ dữ liệu có giá riêng.
     */
    public static BangGiaResponseDTO withCustomPrice(
            Integer loaiPhiId, String tenLoaiPhi, String donViTinh, BigDecimal donGiaMacDinh,
            Integer toaNhaId, String tenToaNha,
            Integer bangGiaId, BigDecimal donGiaRieng, LocalDateTime ngayApDung, String ghiChu) {
        
        BangGiaResponseDTO dto = new BangGiaResponseDTO();
        dto.id = bangGiaId;
        dto.loaiPhiId = loaiPhiId;
        dto.tenLoaiPhi = tenLoaiPhi;
        dto.donViTinh = donViTinh;
        dto.donGiaMacDinh = donGiaMacDinh;
        dto.toaNhaId = toaNhaId;
        dto.tenToaNha = tenToaNha;
        dto.donGiaRieng = donGiaRieng;
        dto.ngayApDung = ngayApDung;
        dto.ghiChu = ghiChu;
        dto.donGiaApDung = donGiaRieng; // Ưu tiên giá riêng
        return dto;
    }

    /**
     * Tạo DTO khi chưa có giá riêng (dùng giá mặc định).
     */
    public static BangGiaResponseDTO withDefaultPrice(
            Integer loaiPhiId, String tenLoaiPhi, String donViTinh, BigDecimal donGiaMacDinh,
            Integer toaNhaId, String tenToaNha) {
        
        BangGiaResponseDTO dto = new BangGiaResponseDTO();
        dto.loaiPhiId = loaiPhiId;
        dto.tenLoaiPhi = tenLoaiPhi;
        dto.donViTinh = donViTinh;
        dto.donGiaMacDinh = donGiaMacDinh;
        dto.toaNhaId = toaNhaId;
        dto.tenToaNha = tenToaNha;
        dto.donGiaRieng = null;
        dto.donGiaApDung = donGiaMacDinh; // Dùng giá mặc định
        return dto;
    }

    // ===== Getters & Setters =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getDonGiaMacDinh() {
        return donGiaMacDinh;
    }

    public void setDonGiaMacDinh(BigDecimal donGiaMacDinh) {
        this.donGiaMacDinh = donGiaMacDinh;
    }

    public Integer getToaNhaId() {
        return toaNhaId;
    }

    public void setToaNhaId(Integer toaNhaId) {
        this.toaNhaId = toaNhaId;
    }

    public String getTenToaNha() {
        return tenToaNha;
    }

    public void setTenToaNha(String tenToaNha) {
        this.tenToaNha = tenToaNha;
    }

    public BigDecimal getDonGiaRieng() {
        return donGiaRieng;
    }

    public void setDonGiaRieng(BigDecimal donGiaRieng) {
        this.donGiaRieng = donGiaRieng;
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

    public BigDecimal getDonGiaApDung() {
        return donGiaApDung;
    }

    public void setDonGiaApDung(BigDecimal donGiaApDung) {
        this.donGiaApDung = donGiaApDung;
    }

    // ===== Helper Methods =====

    /**
     * Kiểm tra có giá riêng không.
     */
    public boolean hasCustomPrice() {
        return donGiaRieng != null;
    }
}
