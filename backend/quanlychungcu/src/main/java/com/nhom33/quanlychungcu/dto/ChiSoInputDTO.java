package com.nhom33.quanlychungcu.dto;

/**
 * DTO: Dữ liệu nhập liệu chỉ số điện nước.
 * Dùng cho API GET /api/chi-so/prepare-input
 * 
 * LOGIC MỚI: Ghi chỉ số theo Tháng/Năm, không tính tiền tại đây
 * Việc tính tiền sẽ được thực hiện khi tạo Đợt thu có phí Điện/Nước
 */
public class ChiSoInputDTO {
    private Integer hoGiaDinhId;
    private String maHoGiaDinh;
    private String tenChuHo;
    private String soCanHo;
    private Integer chiSoCu;      // Chỉ số tháng trước (để hiển thị)
    private Integer chiSoMoi;     // Chỉ số đã nhập (null = chưa nhập)
    private String trangThai;     // "Chưa nhập" / "Đã chốt"

    // Constructors
    public ChiSoInputDTO() {
    }

    public ChiSoInputDTO(Integer hoGiaDinhId, String maHoGiaDinh, String tenChuHo, 
                         String soCanHo, Integer chiSoCu, Integer chiSoMoi) {
        this.hoGiaDinhId = hoGiaDinhId;
        this.maHoGiaDinh = maHoGiaDinh;
        this.tenChuHo = tenChuHo;
        this.soCanHo = soCanHo;
        this.chiSoCu = chiSoCu != null ? chiSoCu : 0;
        this.chiSoMoi = chiSoMoi;
        this.trangThai = chiSoMoi != null ? "Đã chốt" : "Chưa nhập";
    }

    // Getters and Setters
    public Integer getHoGiaDinhId() {
        return hoGiaDinhId;
    }

    public void setHoGiaDinhId(Integer hoGiaDinhId) {
        this.hoGiaDinhId = hoGiaDinhId;
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

    public String getSoCanHo() {
        return soCanHo;
    }

    public void setSoCanHo(String soCanHo) {
        this.soCanHo = soCanHo;
    }

    public Integer getChiSoCu() {
        return chiSoCu;
    }

    public void setChiSoCu(Integer chiSoCu) {
        this.chiSoCu = chiSoCu;
    }

    public Integer getChiSoMoi() {
        return chiSoMoi;
    }

    public void setChiSoMoi(Integer chiSoMoi) {
        this.chiSoMoi = chiSoMoi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
