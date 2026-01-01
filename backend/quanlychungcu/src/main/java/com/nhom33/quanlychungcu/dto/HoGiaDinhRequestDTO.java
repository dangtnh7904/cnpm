package com.nhom33.quanlychungcu.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * DTO cho việc tạo mới Hộ gia đình.
 * Bắt buộc phải có thông tin Chủ hộ (ChuHoRequestDTO).
 * Unique constraint: (MaHoGiaDinh, ID_ToaNha) phải là duy nhất.
 */
public class HoGiaDinhRequestDTO {

    @NotBlank(message = "Mã hộ gia đình không được để trống")
    @Size(max = 20, message = "Mã hộ gia đình không được vượt quá 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Mã hộ gia đình chỉ được chứa chữ cái, số và dấu gạch ngang")
    private String maHoGiaDinh;

    @NotNull(message = "ID tòa nhà không được để trống")
    private Integer idToaNha;

    @NotBlank(message = "Số căn hộ không được để trống")
    @Size(max = 20, message = "Số căn hộ không được vượt quá 20 ký tự")
    private String soCanHo;

    @NotNull(message = "Số tầng không được để trống")
    @Min(value = 1, message = "Số tầng phải lớn hơn 0")
    private Integer soTang;

    @DecimalMin(value = "0.0", inclusive = false, message = "Diện tích phải lớn hơn 0")
    private Double dienTich;

    /**
     * Thông tin Chủ hộ - BẮT BUỘC.
     * Không thể tạo hộ gia đình mà không có chủ hộ.
     */
    @NotNull(message = "Thông tin chủ hộ không được để trống")
    @Valid
    private ChuHoRequestDTO chuHo;

    // Constructors
    public HoGiaDinhRequestDTO() {
    }

    // Getters and Setters
    public String getMaHoGiaDinh() {
        return maHoGiaDinh;
    }

    public void setMaHoGiaDinh(String maHoGiaDinh) {
        this.maHoGiaDinh = maHoGiaDinh;
    }

    public Integer getIdToaNha() {
        return idToaNha;
    }

    public void setIdToaNha(Integer idToaNha) {
        this.idToaNha = idToaNha;
    }

    public String getSoCanHo() {
        return soCanHo;
    }

    public void setSoCanHo(String soCanHo) {
        this.soCanHo = soCanHo;
    }

    public Integer getSoTang() {
        return soTang;
    }

    public void setSoTang(Integer soTang) {
        this.soTang = soTang;
    }

    public Double getDienTich() {
        return dienTich;
    }

    public void setDienTich(Double dienTich) {
        this.dienTich = dienTich;
    }

    public ChuHoRequestDTO getChuHo() {
        return chuHo;
    }

    public void setChuHo(ChuHoRequestDTO chuHo) {
        this.chuHo = chuHo;
    }
}
