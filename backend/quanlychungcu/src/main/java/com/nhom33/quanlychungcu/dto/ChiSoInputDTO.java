package com.nhom33.quanlychungcu.dto;

import java.math.BigDecimal;

/**
 * DTO: Dữ liệu nhập liệu chỉ số điện nước.
 * Dùng cho API GET /api/chi-so/prepare-input
 */
public class ChiSoInputDTO {
    private Integer hoGiaDinhId;
    private String maHoGiaDinh;
    private String tenChuHo;
    private String soCanHo;
    private Integer chiSoCu;
    private Integer chiSoMoi;
    private String trangThai; // "Chưa nhập" / "Đã chốt"
    private BigDecimal donGia;
    private Integer tieuThu;
    private BigDecimal thanhTien;

    // Constructors
    public ChiSoInputDTO() {
    }

    public ChiSoInputDTO(Integer hoGiaDinhId, String maHoGiaDinh, String tenChuHo, 
                         String soCanHo, Integer chiSoCu, Integer chiSoMoi, BigDecimal donGia) {
        this.hoGiaDinhId = hoGiaDinhId;
        this.maHoGiaDinh = maHoGiaDinh;
        this.tenChuHo = tenChuHo;
        this.soCanHo = soCanHo;
        this.chiSoCu = chiSoCu != null ? chiSoCu : 0;
        this.chiSoMoi = chiSoMoi;
        this.donGia = donGia;
        this.trangThai = chiSoMoi != null ? "Đã chốt" : "Chưa nhập";
        
        if (chiSoMoi != null) {
            this.tieuThu = Math.max(0, chiSoMoi - this.chiSoCu);
            this.thanhTien = donGia != null ? donGia.multiply(BigDecimal.valueOf(this.tieuThu)) : BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public Integer getHoGiaDinhId() {
        return hoGiaDinhId;
    }

    public void setHoGiaDinhId(Integer hoGiaDinhId) {
        this.hoGiaDinhId = hoGiaDinhId;
    }

    public String getMaHoGiaDinh() {
        return maHoGiaDinh;
    }

    public void setMaHoGiaDinh(String maHoGiaDinh) {
        this.maHoGiaDinh = maHoGiaDinh;
    }

    public String getTenChuHo() {
        return tenChuHo;
    }

    public void setTenChuHo(String tenChuHo) {
        this.tenChuHo = tenChuHo;
    }

    public String getSoCanHo() {
        return soCanHo;
    }

    public void setSoCanHo(String soCanHo) {
        this.soCanHo = soCanHo;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public Integer getTieuThu() {
        return tieuThu;
    }

    public void setTieuThu(Integer tieuThu) {
        this.tieuThu = tieuThu;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
}
