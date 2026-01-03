package com.nhom33.quanlychungcu.dto;

import com.nhom33.quanlychungcu.entity.DotThuLoaiPhi;
import com.nhom33.quanlychungcu.entity.LoaiPhi;

import java.math.BigDecimal;

/**
 * DTO cho DotThuLoaiPhi với thông tin giá ưu tiên.
 * 
 * Giá ưu tiên được lấy theo thứ tự:
 * 1. BangGiaDichVu (giá riêng theo tòa nhà) - nếu có
 * 2. LoaiPhi.DonGia (giá mặc định) - nếu không có giá riêng
 */
public class DotThuLoaiPhiDTO {
    
    private Integer id;
    private LoaiPhiInfo loaiPhi;
    private BigDecimal donGiaApDung; // Giá ưu tiên (đã tính toán)
    private String nguonGia; // "BangGiaDichVu" hoặc "LoaiPhi"
    
    public DotThuLoaiPhiDTO() {
    }
    
    public DotThuLoaiPhiDTO(DotThuLoaiPhi entity, BigDecimal donGiaApDung, String nguonGia) {
        this.id = entity.getId();
        this.loaiPhi = new LoaiPhiInfo(entity.getLoaiPhi());
        this.donGiaApDung = donGiaApDung;
        this.nguonGia = nguonGia;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public LoaiPhiInfo getLoaiPhi() {
        return loaiPhi;
    }
    
    public void setLoaiPhi(LoaiPhiInfo loaiPhi) {
        this.loaiPhi = loaiPhi;
    }
    
    public BigDecimal getDonGiaApDung() {
        return donGiaApDung;
    }
    
    public void setDonGiaApDung(BigDecimal donGiaApDung) {
        this.donGiaApDung = donGiaApDung;
    }
    
    public String getNguonGia() {
        return nguonGia;
    }
    
    public void setNguonGia(String nguonGia) {
        this.nguonGia = nguonGia;
    }
    
    /**
     * Inner class chứa thông tin LoaiPhi cần thiết.
     */
    public static class LoaiPhiInfo {
        private Integer id;
        private String tenLoaiPhi;
        private BigDecimal donGia; // Giá mặc định (chỉ để tham khảo)
        private String donViTinh;
        private String loaiThu;
        private String moTa;
        
        public LoaiPhiInfo() {
        }
        
        public LoaiPhiInfo(LoaiPhi entity) {
            this.id = entity.getId();
            this.tenLoaiPhi = entity.getTenLoaiPhi();
            this.donGia = entity.getDonGia();
            this.donViTinh = entity.getDonViTinh();
            this.loaiThu = entity.getLoaiThu();
            this.moTa = entity.getMoTa();
        }
        
        // Getters and Setters
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
        
        public String getLoaiThu() {
            return loaiThu;
        }
        
        public void setLoaiThu(String loaiThu) {
            this.loaiThu = loaiThu;
        }
        
        public String getMoTa() {
            return moTa;
        }
        
        public void setMoTa(String moTa) {
            this.moTa = moTa;
        }
    }
}
