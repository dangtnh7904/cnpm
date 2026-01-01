package com.nhom33.quanlychungcu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "TamTru")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TamTru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TamTru")
    private Integer id;

    @NotNull(message = "Hộ gia đình không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_HoGiaDinh", nullable = false)
    private HoGiaDinh hoGiaDinh;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Pattern(regexp = "^[0-9]{12}$", message = "Số CCCD phải có 12 chữ số")
    @Column(name = "SoCCCD")
    private String soCCCD;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "LyDo")
    private String lyDo;

    @Column(name = "NgayDangKy")
    private LocalDateTime ngayDangKy;

    //  Lifecycle Callbacks 
    
    @PrePersist
    protected void onCreate() {
        if (ngayDangKy == null) {
            ngayDangKy = LocalDateTime.now();
        }
    }

    //  Constructors 
    
    public TamTru() {
    }
    
    public TamTru(HoGiaDinh hoGiaDinh, String hoTen, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.hoGiaDinh = hoGiaDinh;
        this.hoTen = hoTen;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    //  Getter & Setter 

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public HoGiaDinh getHoGiaDinh() {
        return hoGiaDinh;
    }

    public void setHoGiaDinh(HoGiaDinh hoGiaDinh) {
        this.hoGiaDinh = hoGiaDinh;
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

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
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

    public LocalDateTime getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDateTime ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    //  Utility Methods 
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TamTru tamTru = (TamTru) o;
        return Objects.equals(id, tamTru.id) && 
               Objects.equals(soCCCD, tamTru.soCCCD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, soCCCD);
    }

    @Override
    public String toString() {
        return "TamTru{" +
                "id=" + id +
                ", hoTen='" + hoTen + '\'' +
                ", soCCCD='" + soCCCD + '\'' +
                ", ngayBatDau=" + ngayBatDau +
                ", ngayKetThuc=" + ngayKetThuc +
                ", ngayDangKy=" + ngayDangKy +
                '}';
    }
}
