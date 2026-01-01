package com.nhom33.quanlychungcu.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO cho việc thêm/cập nhật Nhân khẩu vào Hộ gia đình.
 * 
 * LUỒNG NGHIỆP VỤ:
 * - Khi QuanHeVoiChuHo = "Chủ hộ": Kiểm tra hộ đã có chủ hộ chưa.
 *   + Nếu có -> Ném lỗi BadRequestException
 *   + Nếu chưa -> Lưu và tự động cập nhật TenChuHo trong bảng HoGiaDinh
 * 
 * - KHÔNG cho phép sửa TrangThai qua API update thông thường.
 *   Việc thay đổi trạng thái phải qua API nghiệp vụ riêng (Tạm vắng/Tạm trú).
 */
public class NhanKhauRequestDTO {

    /**
     * ID Hộ gia đình mà nhân khẩu thuộc về - BẮT BUỘC.
     */
    @NotNull(message = "ID hộ gia đình không được để trống")
    private Integer hoGiaDinhId;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String hoTen;

    @NotBlank(message = "Số CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "Số CCCD phải có đúng 12 chữ số")
    private String soCCCD;

    @NotNull(message = "Ngày sinh không được để trống")
    @PastOrPresent(message = "Ngày sinh không được là ngày trong tương lai")
    private LocalDate ngaySinh;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "^(Nam|Nữ)$", message = "Giới tính phải là 'Nam' hoặc 'Nữ'")
    private String gioiTinh;

    @Pattern(regexp = "^(0[0-9]{9,10})?$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    /**
     * Quan hệ với chủ hộ - QUAN TRỌNG.
     * Nếu = "Chủ hộ", hệ thống sẽ kiểm tra hộ đã có chủ hộ chưa.
     * Các giá trị khác: "Vợ/Chồng", "Con", "Bố/Mẹ", "Anh/Chị/Em", "Ông/Bà", "Cháu", "Khác"
     */
    @NotBlank(message = "Quan hệ với chủ hộ không được để trống")
    @Size(max = 50, message = "Quan hệ với chủ hộ không được vượt quá 50 ký tự")
    private String quanHeVoiChuHo;

    private LocalDate ngayChuyenDen;

    // === KHÔNG có trường TrangThai ở đây ===
    // TrangThai được quản lý bởi hệ thống thông qua các API nghiệp vụ riêng

    // Constructors
    public NhanKhauRequestDTO() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQuanHeVoiChuHo() {
        return quanHeVoiChuHo;
    }

    public void setQuanHeVoiChuHo(String quanHeVoiChuHo) {
        this.quanHeVoiChuHo = quanHeVoiChuHo;
    }

    public LocalDate getNgayChuyenDen() {
        return ngayChuyenDen;
    }

    public void setNgayChuyenDen(LocalDate ngayChuyenDen) {
        this.ngayChuyenDen = ngayChuyenDen;
    }
}
