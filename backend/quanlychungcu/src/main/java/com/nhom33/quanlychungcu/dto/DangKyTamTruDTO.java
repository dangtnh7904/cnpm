package com.nhom33.quanlychungcu.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO cho việc đăng ký Tạm trú.
 * Yêu cầu: Hộ gia đình phải tồn tại và không ở trạng thái "Trống"/"Không sử dụng".
 * Tạm trú là người từ nơi khác đến ở tạm tại hộ gia đình (không phải nhân khẩu).
 */
public class DangKyTamTruDTO {

    @NotNull(message = "ID hộ gia đình không được để trống")
    private Integer hoGiaDinhId;

    // === Thông tin cá nhân người tạm trú ===

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String hoTen;

    @NotBlank(message = "Số CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "Số CCCD phải có đúng 12 chữ số")
    private String soCCCD;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate ngaySinh;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "^(Nam|Nữ)$", message = "Giới tính phải là 'Nam' hoặc 'Nữ'")
    private String gioiTinh;

    @Pattern(regexp = "^(0[0-9]{9,10})?$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    @NotBlank(message = "Địa chỉ thường trú không được để trống")
    @Size(max = 200, message = "Địa chỉ không được vượt quá 200 ký tự")
    private String diaChiThuongTru;

    // === Thông tin tạm trú ===

    @NotNull(message = "Ngày bắt đầu tạm trú không được để trống")
    private LocalDate ngayBatDau;

    /**
     * Ngày kết thúc tạm trú dự kiến (có thể null nếu chưa xác định).
     * Nếu có, phải sau ngày bắt đầu.
     */
    private LocalDate ngayKetThuc;

    @NotBlank(message = "Lý do tạm trú không được để trống")
    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
    private String lyDo;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String ghiChu;

    // Constructors
    public DangKyTamTruDTO() {
    }

    // Getters and Setters
    public Integer getHoGiaDinhId() {
        return hoGiaDinhId;
    }

    public void setHoGiaDinhId(Integer hoGiaDinhId) {
        this.hoGiaDinhId = hoGiaDinhId;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoCCCD() {
        return soCCCD;
    }

    public void setSoCCCD(String soCCCD) {
        this.soCCCD = soCCCD;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChiThuongTru() {
        return diaChiThuongTru;
    }

    public void setDiaChiThuongTru(String diaChiThuongTru) {
        this.diaChiThuongTru = diaChiThuongTru;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
