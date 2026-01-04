package com.nhom33.quanlychungcu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ThongBao")
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ThongBao")
    private Integer id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    @Column(name = "TieuDe", nullable = false, length = 200)
    private String tieuDe;

    @Column(name = "NoiDung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Size(max = 100, message = "Người tạo không được vượt quá 100 ký tự")
    @Column(name = "NguoiTao", length = 100)
    private String nguoiTao;

    @Size(max = 50, message = "Loại thông báo không được vượt quá 50 ký tự")
    @Column(name = "LoaiThongBao", length = 50)
    private String loaiThongBao; // 'Cảnh báo', 'Tin tức', 'Phí'

    // Multi-tenancy SaaS: Thông báo BẮT BUỘC thuộc về một tòa nhà
    @NotNull(message = "Tòa nhà không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ToaNha", nullable = false)
    private ToaNha toaNha;

    // Thông báo riêng cho hộ gia đình nào (NULL = thông báo chung)
    @Column(name = "ID_HoGiaDinh")
    private Integer hoGiaDinhId;

    @Column(name = "DaXem")
    private Boolean daXem = false;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }

    // Constructors
    public ThongBao() {
    }

    public ThongBao(String tieuDe, String noiDung, String nguoiTao) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.nguoiTao = nguoiTao;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public String getLoaiThongBao() {
        return loaiThongBao;
    }

    public void setLoaiThongBao(String loaiThongBao) {
        this.loaiThongBao = loaiThongBao;
    }

    public ToaNha getToaNha() {
        return toaNha;
    }

    public void setToaNha(ToaNha toaNha) {
        this.toaNha = toaNha;
    }

    public Integer getHoGiaDinhId() {
        return hoGiaDinhId;
    }

    public void setHoGiaDinhId(Integer hoGiaDinhId) {
        this.hoGiaDinhId = hoGiaDinhId;
    }

    public Boolean getDaXem() {
        return daXem;
    }

    public void setDaXem(Boolean daXem) {
        this.daXem = daXem;
    }
}

