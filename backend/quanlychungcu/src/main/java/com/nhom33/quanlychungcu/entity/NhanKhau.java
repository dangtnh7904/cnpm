package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "NhanKhau")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NhanKhau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NhanKhau")
    private Integer id;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    @Column(name = "HoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Size(max = 10, message = "Giới tính không được vượt quá 10 ký tự")
    @Column(name = "GioiTinh", length = 10)
    private String gioiTinh;

    @NotBlank(message = "Số CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "Số CCCD phải có 12 chữ số")
    @Column(name = "SoCCCD", nullable = false, unique = true, length = 12)
    private String soCCCD;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Column(name = "Email", length = 100)
    private String email;

    @Size(max = 50, message = "Quan hệ với chủ hộ không được vượt quá 50 ký tự")
    @Column(name = "QuanHeVoiChuHo", length = 50)
    private String quanHeVoiChuHo;

    @Column(name = "LaChuHo")
    private Boolean laChuHo = false;

    @Column(name = "NgayChuyenDen")
    private LocalDate ngayChuyenDen;

    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    @Column(name = "TrangThai", length = 50)
    private String trangThai;

    // ===== Relationships =====

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_HoGiaDinh", nullable = false)
    @JsonIgnoreProperties({"danhSachNhanKhau", "danhSachHoaDon", "danhSachDinhMuc", "danhSachPhanAnh"})
    private HoGiaDinh hoGiaDinh;

    @OneToMany(mappedBy = "nhanKhau", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TamVang> danhSachTamVang = new ArrayList<>();

    // ===== Constructors =====

    public NhanKhau() {
    }

    public NhanKhau(String hoTen, HoGiaDinh hoGiaDinh) {
        this.hoTen = hoTen;
        this.hoGiaDinh = hoGiaDinh;
    }

    // ===== Getter & Setter =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
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

    public String getSoCCCD() {
        return soCCCD;
    }

    public void setSoCCCD(String soCCCD) {
        this.soCCCD = soCCCD;
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

    public Boolean getLaChuHo() {
        return laChuHo;
    }

    public void setLaChuHo(Boolean laChuHo) {
        this.laChuHo = laChuHo;
    }

    public LocalDate getNgayChuyenDen() {
        return ngayChuyenDen;
    }

    public void setNgayChuyenDen(LocalDate ngayChuyenDen) {
        this.ngayChuyenDen = ngayChuyenDen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public HoGiaDinh getHoGiaDinh() {
        return hoGiaDinh;
    }

    public void setHoGiaDinh(HoGiaDinh hoGiaDinh) {
        this.hoGiaDinh = hoGiaDinh;
    }

    public List<TamVang> getDanhSachTamVang() {
        return danhSachTamVang;
    }

    public void setDanhSachTamVang(List<TamVang> danhSachTamVang) {
        this.danhSachTamVang = danhSachTamVang;
    }

    // ===== Utility Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NhanKhau nhanKhau = (NhanKhau) o;
        return Objects.equals(id, nhanKhau.id) && 
               Objects.equals(soCCCD, nhanKhau.soCCCD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, soCCCD);
    }

    @Override
    public String toString() {
        return "NhanKhau{" +
                "id=" + id +
                ", hoTen='" + hoTen + '\'' +
                ", soCCCD='" + soCCCD + '\'' +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", laChuHo=" + laChuHo +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
