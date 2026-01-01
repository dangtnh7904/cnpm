package com.nhom33.quanlychungcu.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO cho thông tin Chủ hộ khi tạo mới Hộ gia đình.
 * Chủ hộ là nhân khẩu đầu tiên và bắt buộc phải có khi tạo hộ.
 */
public class ChuHoRequestDTO {

    @NotBlank(message = "Họ tên chủ hộ không được để trống")
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

    @Size(max = 200, message = "Địa chỉ không được vượt quá 200 ký tự")
    private String diaChiThuongTru;

    @Size(max = 100, message = "Dân tộc không được vượt quá 100 ký tự")
    private String danToc;

    @Size(max = 100, message = "Tôn giáo không được vượt quá 100 ký tự")
    private String tonGiao;

    @Size(max = 100, message = "Quốc tịch không được vượt quá 100 ký tự")
    private String quocTich;

    @Size(max = 100, message = "Nghề nghiệp không được vượt quá 100 ký tự")
    private String ngheNghiep;

    @Size(max = 200, message = "Nơi làm việc không được vượt quá 200 ký tự")
    private String noiLamViec;

    // Constructors
    public ChuHoRequestDTO() {
    }

    // Getters and Setters
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

    public String getDanToc() {
        return danToc;
    }

    public void setDanToc(String danToc) {
        this.danToc = danToc;
    }

    public String getTonGiao() {
        return tonGiao;
    }

    public void setTonGiao(String tonGiao) {
        this.tonGiao = tonGiao;
    }

    public String getQuocTich() {
        return quocTich;
    }

    public void setQuocTich(String quocTich) {
        this.quocTich = quocTich;
    }

    public String getNgheNghiep() {
        return ngheNghiep;
    }

    public void setNgheNghiep(String ngheNghiep) {
        this.ngheNghiep = ngheNghiep;
    }

    public String getNoiLamViec() {
        return noiLamViec;
    }

    public void setNoiLamViec(String noiLamViec) {
        this.noiLamViec = noiLamViec;
    }
}
