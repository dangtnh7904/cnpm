package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.entity.HoaDon;
import com.nhom33.quanlychungcu.entity.ThongBao;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import com.nhom33.quanlychungcu.repository.HoaDonRepository;
import com.nhom33.quanlychungcu.repository.ThongBaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final ThongBaoRepository thongBaoRepo;
    private final HoaDonRepository hoaDonRepo;
    private final HoGiaDinhRepository hoGiaDinhRepo;
    private final JavaMailSender mailSender;
    private final InvoiceService invoiceService;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.notification.enabled:false}")
    private boolean emailEnabled;

    public NotificationService(ThongBaoRepository thongBaoRepo,
                              HoaDonRepository hoaDonRepo,
                              HoGiaDinhRepository hoGiaDinhRepo,
                              JavaMailSender mailSender,
                              InvoiceService invoiceService) {
        this.thongBaoRepo = thongBaoRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
        this.mailSender = mailSender;
        this.invoiceService = invoiceService;
    }

    /**
     * Tạo thông báo mới
     */
    @Transactional
    public ThongBao createThongBao(String tieuDe, String noiDung, String nguoiTao, String loaiThongBao) {
        ThongBao thongBao = new ThongBao();
        thongBao.setTieuDe(tieuDe);
        thongBao.setNoiDung(noiDung);
        thongBao.setNguoiTao(nguoiTao);
        thongBao.setLoaiThongBao(loaiThongBao);
        return thongBaoRepo.save(thongBao);
    }

    /**
     * Gửi thông báo nhắc hạn thanh toán
     */
    @Transactional
    public void sendPaymentReminder(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        HoGiaDinh hoGiaDinh = hoaDon.getHoGiaDinh();
        if (hoGiaDinh.getEmailLienHe() == null || hoGiaDinh.getEmailLienHe().isEmpty()) {
            throw new RuntimeException("Hộ gia đình chưa có email");
        }

        String subject = "Nhắc nhở thanh toán hóa đơn #" + hoaDon.getId();
        String content = String.format(
            "Kính gửi %s,\n\n" +
            "Hệ thống xin nhắc nhở quý khách về khoản phí chưa thanh toán:\n\n" +
            "Mã hóa đơn: HD%06d\n" +
            "Đợt thu: %s\n" +
            "Số tiền còn nợ: %,.0f VNĐ\n\n" +
            "Vui lòng thanh toán trước ngày kết thúc đợt thu.\n\n" +
            "Trân trọng,\nBan quản lý chung cư Blue Moon",
            hoGiaDinh.getTenChuHo(),
            hoaDon.getId(),
            hoaDon.getDotThu().getTenDotThu(),
            hoaDon.getSoTienConNo().doubleValue()
        );

        if (emailEnabled) {
            sendEmail(hoGiaDinh.getEmailLienHe(), subject, content);
        }

        // Lưu thông báo vào hệ thống
        createThongBao(subject, content, "Hệ thống", "Cảnh báo");
    }

    /**
     * Gửi thông báo hàng loạt cho các hộ chưa đóng
     */
    @Transactional
    public int sendBulkPaymentReminder(Integer idDotThu) {
        List<HoaDon> hoaDons = hoaDonRepo.findByDotThuId(idDotThu);
        int sentCount = 0;

        for (HoaDon hoaDon : hoaDons) {
            if (hoaDon.getSoTienConNo().compareTo(java.math.BigDecimal.ZERO) > 0) {
                try {
                    sendPaymentReminder(hoaDon.getId());
                    sentCount++;
                } catch (Exception e) {
                    // Log error nhưng tiếp tục gửi cho các hộ khác
                    System.err.println("Lỗi gửi thông báo cho hộ " + hoaDon.getHoGiaDinh().getMaHoGiaDinh() + ": " + e.getMessage());
                }
            }
        }

        return sentCount;
    }

    /**
     * Gửi email
     */
    private void sendEmail(String to, String subject, String content) {
        if (!emailEnabled || fromEmail == null || fromEmail.isEmpty()) {
            System.out.println("Email không được bật hoặc chưa cấu hình. Nội dung:");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Content: " + content);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage(), e);
        }
    }

    /**
     * Gửi hóa đơn qua email
     */
    @Transactional
    public void sendInvoiceByEmail(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        HoGiaDinh hoGiaDinh = hoaDon.getHoGiaDinh();
        if (hoGiaDinh.getEmailLienHe() == null || hoGiaDinh.getEmailLienHe().isEmpty()) {
            throw new RuntimeException("Hộ gia đình chưa có email");
        }

        String subject = "Hóa đơn thanh toán #" + hoaDon.getId();
        String htmlContent = invoiceService.generateInvoiceHtml(idHoaDon);

        // TODO: Gửi email với HTML content và PDF đính kèm
        // Hiện tại chỉ gửi text
        String textContent = String.format(
            "Kính gửi %s,\n\n" +
            "Hệ thống gửi quý khách hóa đơn thanh toán:\n\n" +
            "Mã hóa đơn: HD%06d\n" +
            "Đợt thu: %s\n" +
            "Tổng tiền: %,.0f VNĐ\n\n" +
            "Vui lòng xem chi tiết trong file đính kèm.\n\n" +
            "Trân trọng,\nBan quản lý chung cư Blue Moon",
            hoGiaDinh.getTenChuHo(),
            hoaDon.getId(),
            hoaDon.getDotThu().getTenDotThu(),
            hoaDon.getTongTienPhaiThu().doubleValue()
        );

        if (emailEnabled) {
            sendEmail(hoGiaDinh.getEmailLienHe(), subject, textContent);
        }
    }
}

