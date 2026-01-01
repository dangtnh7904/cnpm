package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.HoaDon;
import com.nhom33.quanlychungcu.entity.ChiTietHoaDon;
import com.nhom33.quanlychungcu.repository.HoaDonRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceService {

    private final HoaDonRepository hoaDonRepo;

    public InvoiceService(HoaDonRepository hoaDonRepo) {
        this.hoaDonRepo = hoaDonRepo;
    }

    /**
     * Sinh hóa đơn PDF (sử dụng iText hoặc Apache PDFBox)
     * Tạm thời trả về HTML, có thể chuyển sang PDF sau
     */
    public String generateInvoiceHtml(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append(".header { text-align: center; margin-bottom: 30px; }");
        html.append(".total { font-weight: bold; font-size: 18px; }");
        html.append("</style></head><body>");

        html.append("<div class='header'>");
        html.append("<h1>HÓA ĐƠN THANH TOÁN</h1>");
        html.append("<p>Chung cư Blue Moon</p>");
        html.append("</div>");

        html.append("<div>");
        html.append("<p><strong>Mã hóa đơn:</strong> HD").append(String.format("%06d", hoaDon.getId())).append("</p>");
        html.append("<p><strong>Hộ gia đình:</strong> ").append(hoaDon.getHoGiaDinh().getMaHoGiaDinh()).append(" - ").append(hoaDon.getHoGiaDinh().getTenChuHo()).append("</p>");
        html.append("<p><strong>Đợt thu:</strong> ").append(hoaDon.getDotThu().getTenDotThu()).append("</p>");
        html.append("<p><strong>Ngày tạo:</strong> ").append(hoaDon.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</p>");
        html.append("</div>");

        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>STT</th>");
        html.append("<th>Loại phí</th>");
        html.append("<th>Số lượng</th>");
        html.append("<th>Đơn giá</th>");
        html.append("<th>Thành tiền</th>");
        html.append("</tr></thead><tbody>");

        List<ChiTietHoaDon> chiTiets = hoaDon.getDanhSachChiTiet();
        int stt = 1;
        for (ChiTietHoaDon ct : chiTiets) {
            html.append("<tr>");
            html.append("<td>").append(stt++).append("</td>");
            html.append("<td>").append(ct.getLoaiPhi().getTenLoaiPhi()).append("</td>");
            html.append("<td>").append(ct.getSoLuong()).append(" ").append(ct.getLoaiPhi().getDonViTinh() != null ? ct.getLoaiPhi().getDonViTinh() : "").append("</td>");
            html.append("<td>").append(formatCurrency(ct.getDonGia())).append("</td>");
            html.append("<td>").append(formatCurrency(ct.getThanhTien())).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");

        html.append("<div style='margin-top: 20px; text-align: right;'>");
        html.append("<p><strong>Tổng tiền phải thu:</strong> ").append(formatCurrency(hoaDon.getTongTienPhaiThu())).append("</p>");
        html.append("<p><strong>Số tiền đã đóng:</strong> ").append(formatCurrency(hoaDon.getSoTienDaDong())).append("</p>");
        html.append("<p class='total'><strong>Còn nợ:</strong> ").append(formatCurrency(hoaDon.getSoTienConNo())).append("</p>");
        html.append("<p><strong>Trạng thái:</strong> ").append(hoaDon.getTrangThai()).append("</p>");
        html.append("</div>");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Sinh PDF từ HTML (cần thêm dependency)
     */
    public byte[] generateInvoicePdf(Integer idHoaDon) throws IOException {
        String html = generateInvoiceHtml(idHoaDon);
        // TODO: Convert HTML to PDF using iText hoặc Apache PDFBox
        // Tạm thời trả về HTML dưới dạng bytes
        return html.getBytes("UTF-8");
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 đ";
        return String.format("%,d đ", amount.longValue());
    }
}

