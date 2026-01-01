package com.nhom33.quanlychungcu.controller;

import com.github.javafaker.Faker;
import com.nhom33.quanlychungcu.entity.*;
import com.nhom33.quanlychungcu.repository.*;
import com.nhom33.quanlychungcu.service.HoaDonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final HoGiaDinhRepository hoGiaDinhRepo;
    private final NhanKhauRepository nhanKhauRepo;
    private final UserAccountRepository userRepo;
    private final LoaiPhiRepository loaiPhiRepo;
    private final DotThuRepository dotThuRepo;
    private final HoaDonRepository hoaDonRepo;
    private final ChiTietHoaDonRepository chiTietRepo;
    private final DinhMucThuRepository dinhMucRepo;
    private final TamTruRepository tamTruRepo;
    private final TamVangRepository tamVangRepo;
    
    private final PasswordEncoder passwordEncoder;
    private final HoaDonService hoaDonService;

    public TestController(HoGiaDinhRepository hoGiaDinhRepo, NhanKhauRepository nhanKhauRepo,
                          UserAccountRepository userRepo, LoaiPhiRepository loaiPhiRepo,
                          DotThuRepository dotThuRepo, HoaDonRepository hoaDonRepo,
                          ChiTietHoaDonRepository chiTietRepo, DinhMucThuRepository dinhMucRepo,
                          TamTruRepository tamTruRepo, TamVangRepository tamVangRepo,
                          PasswordEncoder passwordEncoder, HoaDonService hoaDonService) {
        this.hoGiaDinhRepo = hoGiaDinhRepo;
        this.nhanKhauRepo = nhanKhauRepo;
        this.userRepo = userRepo;
        this.loaiPhiRepo = loaiPhiRepo;
        this.dotThuRepo = dotThuRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.chiTietRepo = chiTietRepo;
        this.dinhMucRepo = dinhMucRepo;
        this.tamTruRepo = tamTruRepo;
        this.tamVangRepo = tamVangRepo;
        this.passwordEncoder = passwordEncoder;
        this.hoaDonService = hoaDonService;
    }

    @PostMapping("/reset-data")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<String> resetAndSeedData() {
        // 1. Clear all data (Order is important to avoid FK constraints)
        tamVangRepo.deleteAll();
        tamTruRepo.deleteAll();
        chiTietRepo.deleteAll();
        hoaDonRepo.deleteAll(); // Delete invoices first
        dinhMucRepo.deleteAll();
        nhanKhauRepo.deleteAll();
        hoGiaDinhRepo.deleteAll();
        dotThuRepo.deleteAll(); // Delete batches
        loaiPhiRepo.deleteAll();
        
        // Note: Users are typically NOT deleted, but for a full reset we might want to?
        // Let's keep existing users or reset to default? The requirement says "don het data".
        // Let's reset users to default only.
        userRepo.deleteAll();

        // 2. Seed Users
        seedUsers();

        // 3. Seed Fee Types (Loai Phi)
        List<LoaiPhi> loaiPhis = seedLoaiPhi();

        // 4. Seed Households (Ho Gia Dinh) & Residents (Nhan Khau)
        List<HoGiaDinh> households = seedHouseholdsAndResidents();

        // 5. Seed Batches (Dot Thu)
        List<DotThu> dotThus = seedDotThu();

        // 6. Generate Invoices
        seedInvoices(households, dotThus, loaiPhis);

        return ResponseEntity.ok("Reset and Seed Data Successful! Created 20 households and sample data.");
    }

    private void seedUsers() {
        // Use {noop} prefix if you don't have a real BCrypt encoder or if you want plain text for dev
        // checking AuthService to see what it uses. It uses BCrypt.
        
        UserAccount admin = new UserAccount();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setFullName("Quản trị viên");
        admin.setEmail("admin@chungcu.com");
        admin.setRole(Role.ADMIN);
        userRepo.save(admin);

        UserAccount accountant = new UserAccount();
        accountant.setUsername("accountant");
        accountant.setPassword(passwordEncoder.encode("Accountant@123")); // Using same pass for ease
        accountant.setFullName("Kế toán trưởng");
        accountant.setEmail("ketoan@chungcu.com");
        accountant.setRole(Role.ACCOUNTANT);
        userRepo.save(accountant);
        
        UserAccount resident = new UserAccount();
        resident.setUsername("resident");
        resident.setPassword(passwordEncoder.encode("Resident@123"));
        resident.setFullName("Cư dân mẫu");
        resident.setEmail("cudan@chungcu.com");
        resident.setRole(Role.RESIDENT);
        userRepo.save(resident);
    }

    private List<LoaiPhi> seedLoaiPhi() {
        LoaiPhi phiDichVu = new LoaiPhi();
        phiDichVu.setTenLoaiPhi("Phí dịch vụ chung cư");
        phiDichVu.setDonGia(BigDecimal.valueOf(5000));
        phiDichVu.setDonViTinh("m2");
        phiDichVu.setLoaiThu("BatBuoc");
        phiDichVu.setMoTa("Phí vệ sinh, an ninh, thang máy");
        
        LoaiPhi phiGuiXe = new LoaiPhi();
        phiGuiXe.setTenLoaiPhi("Phí gửi xe máy");
        phiGuiXe.setDonGia(BigDecimal.valueOf(80000));
        phiGuiXe.setDonViTinh("xe");
        phiGuiXe.setLoaiThu("BatBuoc");
        
        LoaiPhi phiOTo = new LoaiPhi();
        phiOTo.setTenLoaiPhi("Phí gửi ô tô");
        phiOTo.setDonGia(BigDecimal.valueOf(1200000));
        phiOTo.setDonViTinh("xe");
        phiOTo.setLoaiThu("BatBuoc");

        LoaiPhi quyViDan = new LoaiPhi();
        quyViDan.setTenLoaiPhi("Quỹ ủng hộ người nghèo");
        quyViDan.setDonGia(BigDecimal.ZERO); // Tùy tâm
        quyViDan.setDonViTinh("hộ");
        quyViDan.setLoaiThu("TuNguyen");

        return loaiPhiRepo.saveAll(List.of(phiDichVu, phiGuiXe, phiOTo, quyViDan));
    }

    private List<HoGiaDinh> seedHouseholdsAndResidents() {
        Faker faker = new Faker(new Locale("vi"));
        List<HoGiaDinh> households = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            HoGiaDinh hgd = new HoGiaDinh();
            // Generate valid room number like P101, P102... P201...
            int floor = (i - 1) / 5 + 1; // 1, 2, 3, 4
            int room = (i - 1) % 5 + 1;  // 1, 2, 3, 4, 5
            String roomNum = "P" + floor + "0" + room;
            
            hgd.setMaHoGiaDinh("HGD" + String.format("%03d", i));
            hgd.setSoCanHo(roomNum);
            hgd.setSoTang(floor);
            hgd.setDienTich(50.0 + faker.random().nextInt(50)); // 50 - 100m2
            
            String tenChuHo = faker.name().fullName();
            hgd.setTenChuHo(tenChuHo);
            hgd.setSoDienThoaiLienHe(faker.phoneNumber().cellPhone().replaceAll("\\s+", ""));
            // Ensure phone is 10 chars for validation if mostly 09...
            if (hgd.getSoDienThoaiLienHe().length() > 11) { 
                hgd.setSoDienThoaiLienHe("09" + faker.number().digits(8)); 
            }
            
            hgd.setTrangThai("Dang o");
            
            // Save Household first to get ID
            hgd = hoGiaDinhRepo.save(hgd);
            households.add(hgd);

            // Create Residents
            int numResidents = faker.random().nextInt(1, 4);
            List<NhanKhau> residents = new ArrayList<>();
            
            // Create Owner (Chu Ho) matching household name
            NhanKhau owner = new NhanKhau();
            owner.setHoTen(tenChuHo);
            owner.setHoGiaDinh(hgd);
            owner.setLaChuHo(true);
            owner.setGioiTinh(faker.bool().bool() ? "Nam" : "Nữ");
            owner.setNgaySinh(LocalDate.of(1980 + faker.random().nextInt(20), 1, 1));
            owner.setSoCCCD(faker.number().digits(12));
            owner.setQuanHeVoiChuHo("Chủ hộ");
            owner.setTrangThai("Thuong tru");
            residents.add(owner);

            for (int j = 0; j < numResidents - 1; j++) {
                NhanKhau r = new NhanKhau();
                r.setHoTen(faker.name().fullName());
                r.setHoGiaDinh(hgd);
                r.setLaChuHo(false);
                r.setGioiTinh(faker.bool().bool() ? "Nam" : "Nữ");
                r.setNgaySinh(LocalDate.of(2000 + faker.random().nextInt(20), 1, 1));
                r.setSoCCCD(faker.number().digits(12)); // Chance of collision but low
                r.setQuanHeVoiChuHo("Thành viên");
                r.setTrangThai("Thuong tru");
                residents.add(r);
            }
            nhanKhauRepo.saveAll(residents);
            
            // Setup fee quotas (Dinh Muc) for this household
            // Assume ID 2, 3 are parking (from seedLoaiPhi) but ID isn't guaranteed.
            // We'll skip complex logic and just add quotas randomly in `seedInvoices`.
        }
        return households;
    }

    private List<DotThu> seedDotThu() {
        DotThu dt1 = new DotThu();
        dt1.setTenDotThu("Thu phí tháng 11/2025");
        dt1.setLoaiDotThu("PhiSinhHoat");
        dt1.setNgayBatDau(LocalDate.of(2025, 11, 1));
        dt1.setNgayKetThuc(LocalDate.of(2025, 11, 30));
        
        DotThu dt2 = new DotThu();
        dt2.setTenDotThu("Quyên góp ủng hộ lũ lụt");
        dt2.setLoaiDotThu("DongGop");
        dt2.setNgayBatDau(LocalDate.of(2025, 11, 15));
        dt2.setNgayKetThuc(LocalDate.of(2025, 12, 15));
        
        return dotThuRepo.saveAll(List.of(dt1, dt2));
    }

    private void seedInvoices(List<HoGiaDinh> households, List<DotThu> dotThus, List<LoaiPhi> loaiPhis) {
        DotThu phiThang11 = dotThus.get(0);
        
        for (HoGiaDinh h : households) {
            // Generate invoice for Month 11
            HoaDon hd = new HoaDon();
            hd.setHoGiaDinh(h);
            hd.setDotThu(phiThang11);
            hd.setTrangThai("Chưa đóng");
            hd = hoaDonRepo.save(hd); // Save to get ID

            // Calculate details
            BigDecimal total = BigDecimal.ZERO;
            List<ChiTietHoaDon> details = new ArrayList<>();

            // 1. Service Fee (Area * 5000)
            LoaiPhi phiDichVu = loaiPhis.get(0);
            BigDecimal serviceFee = phiDichVu.getDonGia().multiply(BigDecimal.valueOf(h.getDienTich()));
            
            ChiTietHoaDon ct1 = new ChiTietHoaDon();
            ct1.setHoaDon(hd);
            ct1.setLoaiPhi(phiDichVu);
            ct1.setSoLuong(h.getDienTich());
            ct1.setDonGia(phiDichVu.getDonGia());
            ct1.setThanhTien(serviceFee);
            details.add(ct1);
            total = total.add(serviceFee);

            // 2. Parking Fee (Random 1-2 bikes)
            LoaiPhi phiXe = loaiPhis.get(1);
            int bikes = new Random().nextInt(3); // 0, 1, 2
            if (bikes > 0) {
                BigDecimal parkingFee = phiXe.getDonGia().multiply(BigDecimal.valueOf(bikes));
                ChiTietHoaDon ct2 = new ChiTietHoaDon();
                ct2.setHoaDon(hd);
                ct2.setLoaiPhi(phiXe);
                ct2.setSoLuong((double) bikes);
                ct2.setDonGia(phiXe.getDonGia());
                ct2.setThanhTien(parkingFee);
                details.add(ct2);
                total = total.add(parkingFee);
            }

            chiTietRepo.saveAll(details);
            hd.setTongTienPhaiThu(total);
            hd.setSoTienDaDong(BigDecimal.ZERO);
            
            // Randomly mark some as Paid
            if (new Random().nextBoolean()) {
                hd.setTrangThai("Đã đóng");
                hd.setSoTienDaDong(total);
            }
            hoaDonRepo.save(hd);
        }
    }
}
