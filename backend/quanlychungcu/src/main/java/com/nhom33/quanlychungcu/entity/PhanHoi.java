package com.nhom33.quanlychungcu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PhanHoi")
public class PhanHoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PhanHoi")
    private Integer id;

    @NotNull(message = "Phản ánh không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PhanAnh", nullable = false)
    private PhanAnh phanAnh;

    @NotBlank(message = "Nội dung không được để trống")
    @Column(name = "NoiDung", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Size(max = 100, message = "Người trả lời không được vượt quá 100 ký tự")
    @Column(name = "NguoiTraLoi", length = 100)
    private String nguoiTraLoi;

    @Column(name = "NgayTraLoi")
    private LocalDateTime ngayTraLoi;

    @PrePersist
    protected void onCreate() {
        if (ngayTraLoi == null) {
            ngayTraLoi = LocalDateTime.now();
        }
    }

    // Constructors
    public PhanHoi() {
    }

    public PhanHoi(PhanAnh phanAnh, String noiDung, String nguoiTraLoi) {
        this.phanAnh = phanAnh;
        this.noiDung = noiDung;
        this.nguoiTraLoi = nguoiTraLoi;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PhanAnh getPhanAnh() {
        return phanAnh;
    }

    public void setPhanAnh(PhanAnh phanAnh) {
        this.phanAnh = phanAnh;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getNguoiTraLoi() {
        return nguoiTraLoi;
    }

    public void setNguoiTraLoi(String nguoiTraLoi) {
        this.nguoiTraLoi = nguoiTraLoi;
    }

    public LocalDateTime getNgayTraLoi() {
        return ngayTraLoi;
    }

    public void setNgayTraLoi(LocalDateTime ngayTraLoi) {
        this.ngayTraLoi = ngayTraLoi;
    }
}

