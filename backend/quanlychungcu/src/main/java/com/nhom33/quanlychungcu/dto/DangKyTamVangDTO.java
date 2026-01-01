package com.nhom33.quanlychungcu.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO cho việc đăng ký Tạm vắng.
 * Yêu cầu: Nhân khẩu phải đang ở trạng thái "Đang ở"/"Thường trú"/"Hoạt động".
 * Không cho phép đăng ký nếu đang "Tạm vắng"/"Đã chuyển đi"/"Đã mất".
 */
public class DangKyTamVangDTO {

    @NotNull(message = "ID nhân khẩu không được để trống")
    private Integer nhanKhauId;

    @NotNull(message = "Ngày đi không được để trống")
    private LocalDate ngayDi;

    /**
     * Ngày về dự kiến (có thể null nếu chưa xác định).
     * Nếu có, phải sau ngày đi.
     */
    private LocalDate ngayVe;

    @NotBlank(message = "Lý do tạm vắng không được để trống")
    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
    private String lyDo;

    @NotBlank(message = "Nơi đến không được để trống")
    @Size(max = 200, message = "Nơi đến không được vượt quá 200 ký tự")
    private String noiDen;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String ghiChu;

    // Constructors
    public DangKyTamVangDTO() {
    }

    // Getters and Setters
    public Integer getNhanKhauId() {
        return nhanKhauId;
    }

    public void setNhanKhauId(Integer nhanKhauId) {
        this.nhanKhauId = nhanKhauId;
    }

    public LocalDate getNgayDi() {
        return ngayDi;
    }

    public void setNgayDi(LocalDate ngayDi) {
        this.ngayDi = ngayDi;
    }

    public LocalDate getNgayVe() {
        return ngayVe;
    }

    public void setNgayVe(LocalDate ngayVe) {
        this.ngayVe = ngayVe;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getNoiDen() {
        return noiDen;
    }

    public void setNoiDen(String noiDen) {
        this.noiDen = noiDen;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
