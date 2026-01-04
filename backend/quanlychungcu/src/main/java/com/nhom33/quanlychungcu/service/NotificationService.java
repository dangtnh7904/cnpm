package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.entity.HoaDon;
import com.nhom33.quanlychungcu.entity.ThongBao;
import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import com.nhom33.quanlychungcu.repository.HoaDonRepository;
import com.nhom33.quanlychungcu.repository.ThongBaoRepository;
import com.nhom33.quanlychungcu.repository.ToaNhaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import java.util.List;

/**
 * Service quản lý Thông báo.
 * 
 * MULTI-TENANCY SaaS (v4.0):
 * - KHÔNG CÓ thông báo hệ thống nữa (toaNha không thể NULL)
 * - Mọi thông báo PHẢI thuộc về một tòa nhà
 * - ADMIN: Xem tất cả thông báo
 * - MANAGER: Tạo/xem thông báo cho tòa nhà mình quản lý
 * - RESIDENT: Xem thông báo của tòa nhà mình thuộc (qua UserToaNha)
 */
@Service
public class NotificationService {

    private final ThongBaoRepository thongBaoRepo;
    private final HoaDonRepository hoaDonRepo;
    private final HoGiaDinhRepository hoGiaDinhRepo;
    private final ToaNhaRepository toaNhaRepo;
    private final JavaMailSender mailSender;
    private final InvoiceService invoiceService;
    private final SecurityHelper securityHelper;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.notification.enabled:false}")
    private boolean emailEnabled;

    public NotificationService(ThongBaoRepository thongBaoRepo,
                              HoaDonRepository hoaDonRepo,
                              HoGiaDinhRepository hoGiaDinhRepo,
                              ToaNhaRepository toaNhaRepo,
                              JavaMailSender mailSender,
                              InvoiceService invoiceService,
                              SecurityHelper securityHelper) {
        this.thongBaoRepo = thongBaoRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.hoGiaDinhRepo = hoGiaDinhRepo;
        this.toaNhaRepo = toaNhaRepo;
        this.mailSender = mailSender;
        this.invoiceService = invoiceService;
        this.securityHelper = securityHelper;
    }

    /**
     * Tạo thông báo mới (cho Admin - thông báo hệ thống).
     */
    @Transactional
    public ThongBao createThongBao(String tieuDe, String noiDung, String nguoiTao, String loaiThongBao) {
        ThongBao thongBao = new ThongBao();
        thongBao.setTieuDe(tieuDe);
        thongBao.setNoiDung(noiDung);
        thongBao.setNguoiTao(nguoiTao);
        thongBao.setLoaiThongBao(loaiThongBao);
        // toaNha = null -> thông báo hệ thống, tất cả đều thấy
        return thongBaoRepo.save(thongBao);
    }

    /**
     * Tạo thông báo cho tòa nhà (cho Manager).
     */
    @Transactional
    public ThongBao createForBuilding(Integer toaNhaId, String tieuDe, String noiDung, String loaiThongBao) {
        // Kiểm tra quyền
        if (!securityHelper.canManageBuilding(toaNhaId)) {
            throw new AccessDeniedException("Bạn không có quyền tạo thông báo cho tòa nhà này");
        }
        
        ToaNha toaNha = toaNhaRepo.findById(toaNhaId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tòa nhà với ID: " + toaNhaId));
        
        ThongBao thongBao = new ThongBao();
        thongBao.setTieuDe(tieuDe);
        thongBao.setNoiDung(noiDung);
        thongBao.setNguoiTao(securityHelper.getCurrentUser().getUsername());
        thongBao.setLoaiThongBao(loaiThongBao);
        thongBao.setToaNha(toaNha);
        return thongBaoRepo.save(thongBao);
    }

    /**
     * Lấy danh sách thông báo theo quyền:
     * - ADMIN: Xem tất cả
     * - MANAGER/RESIDENT: Xem thông báo của tòa nhà mình
     */
    public Page<ThongBao> findAll(Pageable pageable) {
        if (securityHelper.canViewAll()) {
            return thongBaoRepo.findAll(pageable);
        }
        
        List<Integer> toaNhaIds = securityHelper.getAccessibleBuildingIds();
        if (toaNhaIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        return thongBaoRepo.findByToaNhaIdIn(toaNhaIds, pageable);
    }

    /**
     * Tìm kiếm thông báo.
     */
    public Page<ThongBao> search(String loaiThongBao, String tieuDe, Pageable pageable) {
        if (securityHelper.canViewAll()) {
            return thongBaoRepo.search(loaiThongBao, tieuDe, pageable);
        }
        
        List<Integer> toaNhaIds = securityHelper.getAccessibleBuildingIds();
        if (toaNhaIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        return thongBaoRepo.searchByToaNhaIds(toaNhaIds, loaiThongBao, tieuDe, pageable);
    }

    /**
     * Lấy chi tiết thông báo theo ID.
     */
    public ThongBao getById(Integer id) {
        ThongBao thongBao = thongBaoRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + id));
        
        // Kiểm tra quyền xem
        if (thongBao.getToaNha() != null && !securityHelper.canAccessBuilding(thongBao.getToaNha().getId())) {
            throw new AccessDeniedException("Bạn không có quyền xem thông báo này");
        }
        
        return thongBao;
    }

    /**
     * Xóa thông báo.
     */
    @Transactional
    public void delete(Integer id) {
        ThongBao thongBao = thongBaoRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + id));
        
        // Chỉ Admin hoặc Manager của tòa mới được xóa
        if (thongBao.getToaNha() != null) {
            if (!securityHelper.canManageBuilding(thongBao.getToaNha().getId())) {
                throw new AccessDeniedException("Bạn không có quyền xóa thông báo này");
            }
        } else if (!securityHelper.canViewAll()) {
            throw new AccessDeniedException("Chỉ Admin mới được xóa thông báo hệ thống");
        }
        
        thongBaoRepo.deleteById(id);
    }

    /**
     * Gửi thông báo nhắc hạn cho tòa nhà (đơn giản hóa - chỉ tạo thông báo)
     */
    @Transactional
    public void sendPaymentReminderToBuilding(Integer toaNhaId, String tenDotThu) {
        ToaNha toaNha = toaNhaRepo.findById(toaNhaId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà"));

        String tieuDe = "Nhắc nhở thanh toán - " + tenDotThu;
        String noiDung = String.format(
            "Kính gửi Quý cư dân %s,\n\n" +
            "Ban quản lý xin thông báo nhắc nhở về việc thanh toán các khoản phí cho đợt thu: %s.\n\n" +
            "Vui lòng kiểm tra và thanh toán đúng hạn.\n\n" +
            "Trân trọng,\nBan quản lý",
            toaNha.getTenToaNha(),
            tenDotThu
        );

        // Tạo thông báo cho tòa nhà
        ThongBao thongBao = new ThongBao();
        thongBao.setTieuDe(tieuDe);
        thongBao.setNoiDung(noiDung);
        thongBao.setNguoiTao("Hệ thống");
        thongBao.setLoaiThongBao("Nhắc hạn");
        thongBao.setToaNha(toaNha);
        thongBaoRepo.save(thongBao);
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

