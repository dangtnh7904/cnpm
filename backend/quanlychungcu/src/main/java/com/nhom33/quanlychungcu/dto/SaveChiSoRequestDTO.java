package com.nhom33.quanlychungcu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO: Request lưu chỉ số hàng loạt.
 * Dùng cho API POST /api/chi-so/save-all
 */
public class SaveChiSoRequestDTO {

    @NotNull(message = "Đợt thu không được để trống")
    private Integer dotThuId;

    @NotNull(message = "Loại phí không được để trống")
    private Integer loaiPhiId;

    @NotNull(message = "Danh sách chỉ số không được để trống")
    private List<ChiSoItemDTO> danhSachChiSo;

    // Inner class for each item
    public static class ChiSoItemDTO {
        @NotNull(message = "Hộ gia đình không được để trống")
        private Integer hoGiaDinhId;

        @Min(value = 0, message = "Chỉ số cũ phải >= 0")
        private Integer chiSoCu;

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
    }

    // Getters and Setters
    public Integer getDotThuId() {
        return dotThuId;
    }

    public void setDotThuId(Integer dotThuId) {
        this.dotThuId = dotThuId;
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
