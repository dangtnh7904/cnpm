package com.nhom33.quanlychungcu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "HoGiaDinh")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HoGiaDinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HoGiaDinh")
    private Integer id;

    @NotBlank(message = "Mã hộ gia đình không được để trống")
    @Size(max = 50, message = "Mã hộ gia đình không được vượt quá 50 ký tự")
    @Column(name = "MaHoGiaDinh", nullable = false, unique = true, length = 50)
    private String maHoGiaDinh;

    @Size(max = 100, message = "Tên chủ hộ không được vượt quá 100 ký tự")
    @Column(name = "TenChuHo", length = 100)
    private String tenChuHo;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    @Column(name = "SoDienThoaiLienHe", length = 15)
    private String soDienThoaiLienHe;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Column(name = "EmailLienHe", length = 100)
    private String emailLienHe;

    @Column(name = "SoTang")
    private Integer soTang;

    @Size(max = 50, message = "Số căn hộ không được vượt quá 50 ký tự")
    @Column(name = "SoCanHo", length = 50)
    private String soCanHo;

    @Column(name = "DienTich")
    private Double dienTich;

    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    @Column(name = "TrangThai", length = 50)
    private String trangThai;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;

    // ===== Relationships =====

    @OneToMany(mappedBy = "hoGiaDinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<NhanKhau> danhSachNhanKhau = new ArrayList<>();

    @OneToMany(mappedBy = "hoGiaDinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<TamTru> danhSachTamTru = new ArrayList<>();

    @OneToMany(mappedBy = "hoGiaDinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<HoaDon> danhSachHoaDon = new ArrayList<>();

    @OneToMany(mappedBy = "hoGiaDinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<DinhMucThu> danhSachDinhMuc = new ArrayList<>();

    @OneToMany(mappedBy = "hoGiaDinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<PhanAnh> danhSachPhanAnh = new ArrayList<>();

    // ===== Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }

    // ===== Constructors =====

    public HoGiaDinh() {
    }

    public HoGiaDinh(String maHoGiaDinh, String tenChuHo) {
        this.maHoGiaDinh = maHoGiaDinh;
        this.tenChuHo = tenChuHo;
    }

    // ===== Getter & Setter =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaHoGiaDinh() {
        return maHoGiaDinh;
    }

    public void setMaHoGiaDinh(String maHoGiaDinh) {
        this.maHoGiaDinh = maHoGiaDinh;
    }

    public String getTenChuHo() {
        return tenChuHo;
    }

    public void setTenChuHo(String tenChuHo) {
        this.tenChuHo = tenChuHo;
    }

    public String getSoDienThoaiLienHe() {
        return soDienThoaiLienHe;
    }

    public void setSoDienThoaiLienHe(String soDienThoaiLienHe) {
        this.soDienThoaiLienHe = soDienThoaiLienHe;
    }

    public String getEmailLienHe() {
        return emailLienHe;
    }

    public void setEmailLienHe(String emailLienHe) {
        this.emailLienHe = emailLienHe;
    }

    public Integer getSoTang() {
        return soTang;
    }

    public void setSoTang(Integer soTang) {
        this.soTang = soTang;
    }

    public String getSoCanHo() {
        return soCanHo;
    }

    public void setSoCanHo(String soCanHo) {
        this.soCanHo = soCanHo;
    }

    public Double getDienTich() {
        return dienTich;
    }

    public void setDienTich(Double dienTich) {
        this.dienTich = dienTich;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public List<NhanKhau> getDanhSachNhanKhau() {
        return danhSachNhanKhau;
    }

    public void setDanhSachNhanKhau(List<NhanKhau> danhSachNhanKhau) {
        this.danhSachNhanKhau = danhSachNhanKhau;
    }

    public List<TamTru> getDanhSachTamTru() {
        return danhSachTamTru;
    }

    public void setDanhSachTamTru(List<TamTru> danhSachTamTru) {
        this.danhSachTamTru = danhSachTamTru;
    }

    // ===== Utility Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoGiaDinh that = (HoGiaDinh) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(maHoGiaDinh, that.maHoGiaDinh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, maHoGiaDinh);
    }

    @Override
    public String toString() {
        return "HoGiaDinh{" +
                "id=" + id +
                ", maHoGiaDinh='" + maHoGiaDinh + '\'' +
                ", tenChuHo='" + tenChuHo + '\'' +
                ", soCanHo='" + soCanHo + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
