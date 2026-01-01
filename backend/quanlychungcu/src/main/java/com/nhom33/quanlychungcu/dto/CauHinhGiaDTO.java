package com.nhom33.quanlychungcu.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO: Cấu hình giá dịch vụ cho một tòa nhà.
 * 
 * LOGIC NGHIỆP VỤ:
 * - Cho phép set giá riêng cho nhiều loại phí tại một tòa nhà cùng lúc.
 * - Hỗ trợ bulk upsert: Insert nếu chưa có, Update nếu đã tồn tại.
 * 
 * CÁCH SỬ DỤNG:
 * - Frontend gửi toaNhaId + danh sách [loaiPhiId, donGiaRieng]
 * - Backend upsert vào BangGiaDichVu
 */
public class CauHinhGiaDTO {

    @NotNull(message = "ID tòa nhà không được để trống")
    private Integer toaNhaId;

    private String tenToaNha; // Optional: Để hiển thị

    @NotEmpty(message = "Danh sách giá không được rỗng")
    @Valid
    private List<ChiTietGiaDTO> danhSachGia;

    // ===== Constructors =====

    public CauHinhGiaDTO() {
    }

    public CauHinhGiaDTO(Integer toaNhaId, List<ChiTietGiaDTO> danhSachGia) {
        this.toaNhaId = toaNhaId;
        this.danhSachGia = danhSachGia;
    }

    // ===== Getters & Setters =====

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

    public List<ChiTietGiaDTO> getDanhSachGia() {
        return danhSachGia;
    }

    public void setDanhSachGia(List<ChiTietGiaDTO> danhSachGia) {
        this.danhSachGia = danhSachGia;
    }

    // ===== Helper =====

    public int size() {
        return danhSachGia != null ? danhSachGia.size() : 0;
    }
}
