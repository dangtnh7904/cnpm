package com.nhom33.quanlychungcu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO: Request lưu chỉ số hàng loạt.
 * Dùng cho API POST /api/chi-so/save-all
 * 
 * LOGIC MỚI: Ghi chỉ số theo Tháng/Năm, không phụ thuộc Đợt thu
 */
public class SaveChiSoRequestDTO {

    @NotNull(message = "Tháng không được để trống")
    @Min(value = 1, message = "Tháng phải từ 1-12")
    @Max(value = 12, message = "Tháng phải từ 1-12")
    private Integer thang;

    @NotNull(message = "Năm không được để trống")
    @Min(value = 2000, message = "Năm không hợp lệ")
    private Integer nam;

    @NotNull(message = "Tòa nhà không được để trống")
    private Integer toaNhaId;

    @NotNull(message = "Loại phí không được để trống")
    private Integer loaiPhiId;

    @NotNull(message = "Danh sách chỉ số không được để trống")
    private List<ChiSoItemDTO> danhSachChiSo;

    // Inner class for each item
    public static class ChiSoItemDTO {
        @NotNull(message = "Hộ gia đình không được để trống")
        private Integer hoGiaDinhId;

        @NotNull(message = "Chỉ số mới không được để trống")
        @Min(value = 0, message = "Chỉ số mới phải >= 0")
        private Integer chiSoMoi;

        // Getters and Setters
        public Integer getHoGiaDinhId() {
            return hoGiaDinhId;
        }

        public void setHoGiaDinhId(Integer hoGiaDinhId) {
            this.hoGiaDinhId = hoGiaDinhId;
        }

        public Integer getChiSoMoi() {
            return chiSoMoi;
        }

        public void setChiSoMoi(Integer chiSoMoi) {
            this.chiSoMoi = chiSoMoi;
        }
    }

    // Getters and Setters
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

    public Integer getToaNhaId() {
        return toaNhaId;
    }

    public void setToaNhaId(Integer toaNhaId) {
        this.toaNhaId = toaNhaId;
    }

    public Integer getLoaiPhiId() {
        return loaiPhiId;
    }

    public void setLoaiPhiId(Integer loaiPhiId) {
        this.loaiPhiId = loaiPhiId;
    }

    public List<ChiSoItemDTO> getDanhSachChiSo() {
        return danhSachChiSo;
    }

    public void setDanhSachChiSo(List<ChiSoItemDTO> danhSachChiSo) {
        this.danhSachChiSo = danhSachChiSo;
    }
}
