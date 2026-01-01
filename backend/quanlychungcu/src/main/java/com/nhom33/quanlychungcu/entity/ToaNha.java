package com.nhom33.quanlychungcu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "ToaNha")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ToaNha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ToaNha")
    private Integer id;

    @NotBlank(message = "Tên tòa nhà không được để trống")
    @Size(max = 50, message = "Tên tòa nhà không được vượt quá 50 ký tự")
    @Column(name = "TenToaNha", nullable = false, length = 50)
    private String tenToaNha;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    @Column(name = "MoTa", length = 255)
    private String moTa;

    // ===== Relationships =====

    @OneToMany(mappedBy = "toaNha", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoGiaDinh> danhSachHoGiaDinh = new ArrayList<>();

    // ===== Constructors =====

    public ToaNha() {
    }

    public ToaNha(String tenToaNha) {
        this.tenToaNha = tenToaNha;
    }

    public ToaNha(String tenToaNha, String moTa) {
        this.tenToaNha = tenToaNha;
        this.moTa = moTa;
    }

    // ===== Getter & Setter =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenToaNha() {
        return tenToaNha;
    }

    public void setTenToaNha(String tenToaNha) {
        this.tenToaNha = tenToaNha;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public List<HoGiaDinh> getDanhSachHoGiaDinh() {
        return danhSachHoGiaDinh;
    }

    public void setDanhSachHoGiaDinh(List<HoGiaDinh> danhSachHoGiaDinh) {
        this.danhSachHoGiaDinh = danhSachHoGiaDinh;
    }

    // ===== Utility Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToaNha toaNha = (ToaNha) o;
        return Objects.equals(id, toaNha.id) &&
               Objects.equals(tenToaNha, toaNha.tenToaNha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tenToaNha);
    }

    @Override
    public String toString() {
        return "ToaNha{" +
                "id=" + id +
                ", tenToaNha='" + tenToaNha + '\'' +
                ", moTa='" + moTa + '\'' +
                '}';
    }
}
