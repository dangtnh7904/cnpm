-- Active: 1767516438826@@127.0.0.1@1433@QuanLyChungCuDB
-- Active: 1762183366540@@127.0.0.1@1433@master
-- ============================================================================
-- DATABASE SETUP SCRIPT: QuanLyChungCuDB
-- Ngày tạo: 2026-01-04
-- Phiên bản: 4.1 (Multi-Tenancy SaaS - Phí chung của Manager)
-- ============================================================================
-- CHANGELOG:
-- v4.1: Phí chung của Manager
--   - LoaiPhi.ID_NguoiQuanLy: Manager sở hữu loại phí
--   - LoaiPhi.ID_ToaNha có thể NULL (phí CHUNG) hoặc NOT NULL (phí RIÊNG)
--   - Phí CHUNG áp dụng cho tất cả tòa của Manager
--   - Giá riêng cho từng tòa qua BangGiaDichVu
--   - Thêm seed data: ToaNha, HoGiaDinh, NhanKhau, LoaiPhi, BangGiaDichVu
-- v4.0: Multi-Tenancy SaaS hoàn chỉnh - DỮ LIỆU TÁCH BIỆT HOÀN TOÀN
--   - LoaiPhi.ID_ToaNha BẮT BUỘC NOT NULL (không còn phí hệ thống)
--   - ThongBao.ID_ToaNha BẮT BUỘC NOT NULL
--   - Mỗi Manager có phí riêng, đợt thu riêng, thông báo riêng
--   - Manager A KHÔNG thấy dữ liệu của Manager B
-- v3.2: Multi-Tenancy hoàn chỉnh
--   - MaHoGiaDinh UNIQUE trong từng tòa nhà (không phải toàn hệ thống)
--   - NhanKhau được lọc theo tòa nhà của Manager
-- v3.0: Hỗ trợ Multi-Tenancy (SaaS)
--   - ToaNha.ID_NguoiQuanLy: Liên kết tòa nhà với người quản lý
--   - UserToaNha: Liên kết user với tòa nhà
-- ============================================================================

USE master;
GO

-- Drop database nếu tồn tại
DECLARE @DatabaseName nvarchar(50) = N'QuanLyChungCuDB';
IF EXISTS (SELECT name FROM sys.databases WHERE name = @DatabaseName)
BEGIN
    DECLARE @SQL nvarchar(max);
    SET @SQL = N'ALTER DATABASE ' + @DatabaseName + N' SET SINGLE_USER WITH ROLLBACK IMMEDIATE;';
    EXEC(@SQL);
    SET @SQL = N'DROP DATABASE ' + @DatabaseName + N';';
    EXEC(@SQL);
    PRINT 'Database ' + @DatabaseName + ' đã được xóa.';
END

-- Tạo database mới
CREATE DATABASE QuanLyChungCuDB;
GO

USE QuanLyChungCuDB;
GO

-- ============================================================================
-- PHẦN 1: QUẢN LÝ NGƯỜI DÙNG (TẠO TRƯỚC VÌ ĐƯỢC THAM CHIẾU)
-- ============================================================================

-- Bảng Users (Đơn giản - không liên kết với HoGiaDinh)
CREATE TABLE Users (
    ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    Username NVARCHAR(100) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(200) NOT NULL,
    FullName NVARCHAR(100),
    Email NVARCHAR(150),
    Role NVARCHAR(50) NOT NULL,  -- 'ADMIN', 'MANAGER', 'ACCOUNTANT', 'RESIDENT'
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- ============================================================================
-- PHẦN 2: QUẢN LÝ TÒA NHÀ VÀ HỘ GIA ĐÌNH
-- ============================================================================

-- Bảng Tòa nhà (Có liên kết với người quản lý)
CREATE TABLE ToaNha (
    ID_ToaNha INT IDENTITY(1,1) PRIMARY KEY,
    TenToaNha NVARCHAR(50) NOT NULL,
    MoTa NVARCHAR(255),
    ID_NguoiQuanLy INT NULL,  -- Người quản lý tòa nhà này
    CONSTRAINT FK_ToaNha_NguoiQuanLy FOREIGN KEY (ID_NguoiQuanLy) REFERENCES Users(ID)
);

-- Bảng Hộ gia đình
CREATE TABLE HoGiaDinh (
    ID_HoGiaDinh INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_ToaNha INT NOT NULL,
    MaHoGiaDinh NVARCHAR(50) NOT NULL,
    TenChuHo NVARCHAR(100),
    SoDienThoaiLienHe VARCHAR(15),
    EmailLienHe VARCHAR(100),
    SoTang INT,
    SoCanHo NVARCHAR(50),
    DienTich FLOAT, 
    TrangThai NVARCHAR(50),
    NgayTao DATETIME DEFAULT GETDATE(),
    NgayCapNhat DATETIME,
    CONSTRAINT FK_HoGiaDinh_ToaNha FOREIGN KEY (ID_ToaNha) REFERENCES ToaNha(ID_ToaNha),
    -- UNIQUE trong từng tòa nhà (Multi-tenancy): 2 tòa khác nhau có thể có cùng MaHoGiaDinh
    CONSTRAINT UC_HoGiaDinh_MaHoGiaDinh_ToaNha UNIQUE (MaHoGiaDinh, ID_ToaNha)
);

CREATE INDEX IX_HoGiaDinh_ToaNha ON HoGiaDinh(ID_ToaNha);

-- Bảng liên kết User với Tòa nhà
-- LOGIC NGHIỆP VỤ:
-- - Manager gắn user (bằng username) vào tòa nhà mình quản lý
-- - User có thể thuộc nhiều tòa nhà
-- - Dùng để xác định user xem được thông báo của tòa nào
CREATE TABLE UserToaNha (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    ID_User INT NOT NULL,
    ID_ToaNha INT NOT NULL,
    NgayThem DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_UserToaNha_User FOREIGN KEY (ID_User) REFERENCES Users(ID) ON DELETE CASCADE,
    CONSTRAINT FK_UserToaNha_ToaNha FOREIGN KEY (ID_ToaNha) REFERENCES ToaNha(ID_ToaNha) ON DELETE CASCADE,
    CONSTRAINT UQ_UserToaNha UNIQUE (ID_User, ID_ToaNha)
);

CREATE INDEX IX_UserToaNha_User ON UserToaNha(ID_User);
CREATE INDEX IX_UserToaNha_ToaNha ON UserToaNha(ID_ToaNha);


-- ============================================================================
-- PHẦN 3: QUẢN LÝ NHÂN KHẨU
-- ============================================================================

-- Bảng Nhân khẩu
CREATE TABLE NhanKhau (
    ID_NhanKhau INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    HoTen NVARCHAR(100) NOT NULL,
    NgaySinh DATE,
    GioiTinh NVARCHAR(10),
    SoCCCD VARCHAR(12) NOT NULL,
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    QuanHeVoiChuHo NVARCHAR(50),
    LaChuHo BIT DEFAULT 0,
    NgayChuyenDen DATE,
    TrangThai NVARCHAR(50),
    CONSTRAINT FK_NhanKhau_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh),
    CONSTRAINT UC_NhanKhau_SoCCCD UNIQUE (SoCCCD)
);

-- Bảng Tạm trú (Liên kết với NhanKhau)
-- LOGIC NGHIỆP VỤ:
-- - Tạm trú = Người từ nơi khác đến ở tạm tại hộ gia đình
-- - Người tạm trú BẮT BUỘC phải xuất hiện trong danh sách nhân khẩu của hộ
-- - Quy trình: INSERT NhanKhau (TrangThai="Tạm trú") -> INSERT TamTru
CREATE TABLE TamTru (
    ID_TamTru INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_NhanKhau INT NOT NULL,
    MaGiayTamTru NVARCHAR(50) NULL,
    DiaChiThuongTru NVARCHAR(200) NULL,
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NULL,
    LyDo NVARCHAR(500) NULL,
    NgayDangKy DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_TamTru_NhanKhau FOREIGN KEY (ID_NhanKhau) REFERENCES NhanKhau(ID_NhanKhau) ON DELETE CASCADE
);

-- Bảng Tạm vắng
-- LOGIC NGHIỆP VỤ:
-- - Tạm vắng = Người ĐÃ LÀ THÀNH VIÊN của hộ đi vắng tạm thời
-- - Quy trình: UPDATE NhanKhau.TrangThai="Tạm vắng" -> INSERT TamVang
CREATE TABLE TamVang (
    ID_TamVang INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_NhanKhau INT NOT NULL,
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NULL,
    NoiDen NVARCHAR(255),
    LyDo NVARCHAR(500),
    NgayDangKy DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_TamVang_NhanKhau FOREIGN KEY (ID_NhanKhau) REFERENCES NhanKhau(ID_NhanKhau) ON DELETE CASCADE
);

-- Index cho Tạm trú/Tạm vắng
CREATE INDEX IX_TamTru_NhanKhau ON TamTru(ID_NhanKhau);
CREATE INDEX IX_TamTru_NgayBatDau ON TamTru(NgayBatDau);
CREATE INDEX IX_TamVang_NhanKhau ON TamVang(ID_NhanKhau);
CREATE INDEX IX_TamVang_NgayBatDau ON TamVang(NgayBatDau);

-- ============================================================================
-- PHẦN 4: QUẢN LÝ PHÍ VÀ HÓA ĐƠN
-- ============================================================================

-- Bảng Loại phí - Thuộc về Manager, có thể là phí CHUNG hoặc phí RIÊNG
-- MULTI-TENANCY v4.1:
-- - ID_NguoiQuanLy: Manager sở hữu loại phí
-- - ID_ToaNha = NULL: Phí CHUNG (áp dụng cho tất cả tòa của Manager)
-- - ID_ToaNha != NULL: Phí RIÊNG chỉ cho tòa đó
-- - Giá riêng cho từng tòa qua bảng BangGiaDichVu
CREATE TABLE LoaiPhi (
    ID_LoaiPhi INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TenLoaiPhi NVARCHAR(100) NOT NULL,
    DonGia DECIMAL(18, 0) DEFAULT 0,
    DonViTinh NVARCHAR(50),
    LoaiThu NVARCHAR(50) NOT NULL,    -- 'BatBuoc' hoặc 'TuNguyen'
    MoTa NVARCHAR(255),
    DangHoatDong BIT DEFAULT 1,
    ID_NguoiQuanLy INT NOT NULL,  -- Manager sở hữu loại phí này
    ID_ToaNha INT NULL,  -- NULL = Phí CHUNG, NOT NULL = Phí RIÊNG của tòa
    CONSTRAINT FK_LoaiPhi_NguoiQuanLy FOREIGN KEY (ID_NguoiQuanLy) REFERENCES Users(ID),
    CONSTRAINT FK_LoaiPhi_ToaNha FOREIGN KEY (ID_ToaNha) REFERENCES ToaNha(ID_ToaNha) ON DELETE SET NULL
);

CREATE INDEX IX_LoaiPhi_NguoiQuanLy ON LoaiPhi(ID_NguoiQuanLy);
CREATE INDEX IX_LoaiPhi_ToaNha ON LoaiPhi(ID_ToaNha);
CREATE INDEX IX_LoaiPhi_NguoiQuanLy_ToaNha ON LoaiPhi(ID_NguoiQuanLy, ID_ToaNha);

-- Bảng Bảng giá dịch vụ (Giá riêng cho từng tòa nhà)
-- LOGIC NGHIỆP VỤ:
-- - Mỗi tòa nhà có thể có mức giá riêng cho từng loại phí
-- - Nếu không có bản ghi trong bảng này -> dùng DonGia mặc định từ LoaiPhi
-- - Ưu tiên: BangGiaDichVu > LoaiPhi.DonGia
CREATE TABLE BangGiaDichVu (
    ID_BangGia INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_LoaiPhi INT NOT NULL,
    ID_ToaNha INT NOT NULL,
    DonGia DECIMAL(18, 0) NOT NULL,
    NgayApDung DATETIME DEFAULT GETDATE(),
    GhiChu NVARCHAR(255),
    CONSTRAINT FK_BangGia_LoaiPhi FOREIGN KEY (ID_LoaiPhi) REFERENCES LoaiPhi(ID_LoaiPhi),
    CONSTRAINT FK_BangGia_ToaNha FOREIGN KEY (ID_ToaNha) REFERENCES ToaNha(ID_ToaNha),
    CONSTRAINT UC_BangGia_Unique UNIQUE (ID_LoaiPhi, ID_ToaNha)
);

CREATE INDEX IX_BangGia_ToaNha ON BangGiaDichVu(ID_ToaNha);
CREATE INDEX IX_BangGia_LoaiPhi ON BangGiaDichVu(ID_LoaiPhi);

-- Bảng Đợt thu (Mỗi đợt thu thuộc về một tòa nhà + Kỳ hạch toán)
CREATE TABLE DotThu (
    ID_DotThu INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_ToaNha INT NULL,
    TenDotThu NVARCHAR(100) NOT NULL,
    LoaiDotThu NVARCHAR(50),
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    Thang INT NULL,  -- Tháng hạch toán (1-12)
    Nam INT NULL,    -- Năm hạch toán
    NgayTao DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_DotThu_ToaNha FOREIGN KEY (ID_ToaNha) REFERENCES ToaNha(ID_ToaNha),
    CONSTRAINT CK_DotThu_Thang CHECK (Thang IS NULL OR (Thang >= 1 AND Thang <= 12)),
    CONSTRAINT CK_DotThu_Nam CHECK (Nam IS NULL OR (Nam >= 2000 AND Nam <= 2100))
);

CREATE INDEX IX_DotThu_ToaNha ON DotThu(ID_ToaNha);

-- Bảng Định mức thu
CREATE TABLE DinhMucThu (
    ID_DinhMuc INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    ID_LoaiPhi INT NOT NULL,
    SoLuong FLOAT DEFAULT 1,
    GhiChu NVARCHAR(255),
    CONSTRAINT FK_DinhMuc_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh),
    CONSTRAINT FK_DinhMuc_LoaiPhi FOREIGN KEY (ID_LoaiPhi) REFERENCES LoaiPhi(ID_LoaiPhi)
);

-- Bảng Hóa đơn
CREATE TABLE HoaDon (
    ID_HoaDon INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    ID_DotThu INT NOT NULL,
    TongTienPhaiThu DECIMAL(18, 0) DEFAULT 0,
    SoTienDaDong DECIMAL(18, 0) DEFAULT 0,
    TrangThai NVARCHAR(50) DEFAULT N'Chưa đóng',
    NgayTao DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_HoaDon_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh),
    CONSTRAINT FK_HoaDon_DotThu FOREIGN KEY (ID_DotThu) REFERENCES DotThu(ID_DotThu),
    CONSTRAINT UQ_HoaDon_HoGiaDinh_DotThu UNIQUE (ID_HoGiaDinh, ID_DotThu) -- Mỗi hộ chỉ có 1 hóa đơn/đợt thu
);

CREATE INDEX IX_HoaDon_HoGiaDinh ON HoaDon(ID_HoGiaDinh);
CREATE INDEX IX_HoaDon_DotThu ON HoaDon(ID_DotThu);
CREATE INDEX IX_HoaDon_TrangThai ON HoaDon(TrangThai);

-- Bảng Chi tiết hóa đơn
CREATE TABLE ChiTietHoaDon (
    ID_ChiTiet INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoaDon INT NOT NULL,
    ID_LoaiPhi INT NOT NULL,
    SoLuong FLOAT,
    DonGia DECIMAL(18, 0),
    ThanhTien DECIMAL(18, 0),
    CONSTRAINT FK_ChiTiet_HoaDon FOREIGN KEY (ID_HoaDon) REFERENCES HoaDon(ID_HoaDon) ON DELETE CASCADE,
    CONSTRAINT FK_ChiTiet_LoaiPhi FOREIGN KEY (ID_LoaiPhi) REFERENCES LoaiPhi(ID_LoaiPhi),
    CONSTRAINT UQ_ChiTiet_HoaDon_LoaiPhi UNIQUE (ID_HoaDon, ID_LoaiPhi) -- Mỗi loại phí chỉ xuất hiện 1 lần/hóa đơn
);

CREATE INDEX IX_ChiTiet_HoaDon ON ChiTietHoaDon(ID_HoaDon);

-- Bảng Lịch sử thanh toán (Có CHECK constraint cho hình thức thanh toán)
CREATE TABLE LichSuThanhToan (
    ID_GiaoDich INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoaDon INT NOT NULL,
    SoTien DECIMAL(18, 0) NOT NULL,
    NgayNop DATETIME DEFAULT GETDATE(),
    HinhThuc NVARCHAR(50) NOT NULL,  -- Bắt buộc chọn
    NguoiNop NVARCHAR(100),
    GhiChu NVARCHAR(255),
    MaGiaoDichVNPAY VARCHAR(50),
    MaNganHang VARCHAR(20),
    MaPhanHoi VARCHAR(10),
    CONSTRAINT FK_ThanhToan_HoaDon FOREIGN KEY (ID_HoaDon) REFERENCES HoaDon(ID_HoaDon),
    CONSTRAINT CK_HinhThucThanhToan CHECK (HinhThuc IN (N'TIEN_MAT', N'CHUYEN_KHOAN', N'VNPAY'))
);

-- ============================================================================
-- PHẦN 5: QUẢN LÝ ĐIỆN NƯỚC
-- ============================================================================

-- Bảng Ghi chỉ số điện nước (Tách rời khỏi Đợt Thu)
-- LOGIC NGHIỆP VỤ:
-- - Ghi chỉ số là hoạt động cố định hàng tháng (chốt ngày 24)
-- - Lưu theo Tháng/Năm và Hộ gia đình
-- - Khi tạo Đợt thu có phí Điện/Nước: Hệ thống tìm chỉ số tương ứng để tính tiền
CREATE TABLE ChiSoDienNuoc (
    ID_ChiSo INT IDENTITY(1,1) PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    ID_LoaiPhi INT NOT NULL,
    Thang INT NOT NULL,
    Nam INT NOT NULL,
    ChiSoMoi INT NOT NULL,
    NgayChot DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_ChiSo_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh) ON DELETE CASCADE,
    CONSTRAINT FK_ChiSo_LoaiPhi FOREIGN KEY (ID_LoaiPhi) REFERENCES LoaiPhi(ID_LoaiPhi) ON DELETE CASCADE,
    CONSTRAINT UQ_ChiSo_ThangNam UNIQUE (ID_HoGiaDinh, ID_LoaiPhi, Thang, Nam),
    CONSTRAINT CK_ChiSo_Thang CHECK (Thang >= 1 AND Thang <= 12),
    CONSTRAINT CK_ChiSo_Nam CHECK (Nam >= 2000 AND Nam <= 2100)
);

CREATE INDEX IX_ChiSo_HoGiaDinh_LoaiPhi ON ChiSoDienNuoc(ID_HoGiaDinh, ID_LoaiPhi);
CREATE INDEX IX_ChiSo_ThangNam ON ChiSoDienNuoc(Thang, Nam);

-- Bảng cấu hình các khoản phí trong Đợt Thu
CREATE TABLE DotThu_LoaiPhi (
    ID_Config INT IDENTITY(1,1) PRIMARY KEY,
    ID_DotThu INT NOT NULL,
    ID_LoaiPhi INT NOT NULL,
    CONSTRAINT FK_DotThuLoaiPhi_DotThu FOREIGN KEY (ID_DotThu) REFERENCES DotThu(ID_DotThu) ON DELETE CASCADE,
    CONSTRAINT FK_DotThuLoaiPhi_LoaiPhi FOREIGN KEY (ID_LoaiPhi) REFERENCES LoaiPhi(ID_LoaiPhi) ON DELETE CASCADE,
    CONSTRAINT UQ_DotThu_LoaiPhi UNIQUE (ID_DotThu, ID_LoaiPhi)
);

-- ============================================================================
-- PHẦN 6: QUẢN LÝ PHẢN ÁNH VÀ THÔNG BÁO
-- ============================================================================

-- Bảng Phản ánh (User gửi phản ánh cho tòa nhà mình thuộc)
CREATE TABLE PhanAnh (
    ID_PhanAnh INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_User INT NOT NULL,           -- User gửi phản ánh
    ID_ToaNha INT NOT NULL,         -- Tòa nhà nhận phản ánh
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(MAX) NOT NULL,
    NgayGui DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Chờ xử lý',
    CONSTRAINT FK_PhanAnh_User FOREIGN KEY (ID_User) REFERENCES Users(ID),
    CONSTRAINT FK_PhanAnh_ToaNha FOREIGN KEY (ID_ToaNha) REFERENCES ToaNha(ID_ToaNha)
);

-- Bảng Phản hồi
CREATE TABLE PhanHoi (
    ID_PhanHoi INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_PhanAnh INT NOT NULL,
    NoiDung NVARCHAR(MAX) NOT NULL,
    NguoiTraLoi NVARCHAR(100),
    NgayTraLoi DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_PhanHoi_PhanAnh FOREIGN KEY (ID_PhanAnh) REFERENCES PhanAnh(ID_PhanAnh)
);

-- Bảng Thông báo - BẮT BUỘC thuộc về một tòa nhà
-- MULTI-TENANCY: Thông báo tách biệt hoàn toàn giữa các tòa
CREATE TABLE ThongBao (
    ID_ThongBao INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(MAX),
    NgayTao DATETIME DEFAULT GETDATE(),
    NguoiTao NVARCHAR(100),
    LoaiThongBao NVARCHAR(50),
    ID_HoGiaDinh INT NULL,  -- Thông báo riêng cho 1 hộ (optional)
    ID_ToaNha INT NOT NULL, -- BẮT BUỘC: Thông báo PHẢI thuộc về một tòa nhà
    DaXem BIT DEFAULT 0,
    CONSTRAINT FK_ThongBao_ToaNha FOREIGN KEY (ID_ToaNha) REFERENCES ToaNha(ID_ToaNha) ON DELETE CASCADE
);

CREATE INDEX IX_ThongBao_ToaNha ON ThongBao(ID_ToaNha);

-- ============================================================================
-- PHẦN 7: VIEWS (BÁO CÁO)
-- ============================================================================

-- View: Thống kê tình hình thu phí theo Đợt thu
CREATE VIEW View_ThongKeDotThu AS
SELECT 
    DT.ID_DotThu,
    DT.TenDotThu,
    COUNT(HD.ID_HoaDon) AS TongSoHo,
    SUM(HD.TongTienPhaiThu) AS TongPhaiThu,
    SUM(HD.SoTienDaDong) AS TongDaThu,
    CAST(SUM(HD.SoTienDaDong) * 100.0 / NULLIF(SUM(HD.TongTienPhaiThu), 0) AS DECIMAL(5,2)) AS TyLeHoanThanh,
    SUM(CASE WHEN HD.TrangThai = N'Chưa đóng' THEN 1 ELSE 0 END) AS SoHoChuaDong
FROM DotThu DT
LEFT JOIN HoaDon HD ON DT.ID_DotThu = HD.ID_DotThu
GROUP BY DT.ID_DotThu, DT.TenDotThu;
GO

-- View: Thống kê công nợ từng hộ gia đình
CREATE VIEW View_ThongKeCongNoHoGiaDinh AS
SELECT 
    HGD.MaHoGiaDinh,
    HGD.TenChuHo,
    HD.ID_DotThu,
    DT.TenDotThu,
    HD.TongTienPhaiThu,
    HD.SoTienDaDong,
    (HD.TongTienPhaiThu - HD.SoTienDaDong) AS SoTienConNo,
    HD.TrangThai
FROM HoGiaDinh HGD
JOIN HoaDon HD ON HGD.ID_HoGiaDinh = HD.ID_HoGiaDinh
JOIN DotThu DT ON HD.ID_DotThu = DT.ID_DotThu;
GO

-- ============================================================================
-- PHẦN 8: SEED DATA (TÀI KHOẢN MẪU)
-- ============================================================================

-- User mẫu
INSERT INTO Users (Username, PasswordHash, FullName, Email, Role)
VALUES
    ('admin', '{noop}Admin@123', N'Quản lý hệ thống', 'admin@example.com', 'ADMIN'),
    ('manager', '{noop}Manager@123', N'Quản lý tòa nhà A', 'manager@example.com', 'MANAGER'),
    ('manager2', '{noop}Manager@123', N'Quản lý tòa nhà B', 'manager2@example.com', 'MANAGER'),
    ('accountant', '{noop}Accountant@123', N'Kế toán', 'accountant@example.com', 'ACCOUNTANT'),
    ('resident', '{noop}Resident@123', N'Cư dân mẫu', 'resident@example.com', 'RESIDENT');
GO

-- Tòa nhà mẫu (gắn với Manager)
INSERT INTO ToaNha (TenToaNha, MoTa, ID_NguoiQuanLy)
VALUES 
    (N'Tòa A', N'Tòa nhà A - 20 tầng, 200 căn hộ', 2),  -- manager (ID=2)
    (N'Tòa B', N'Tòa nhà B - 15 tầng, 150 căn hộ', 3);  -- manager2 (ID=3)
GO

-- Gắn user vào tòa nhà (UserToaNha)
INSERT INTO UserToaNha (ID_User, ID_ToaNha)
VALUES 
    (2, 1),  -- manager -> Tòa A
    (3, 2),  -- manager2 -> Tòa B
    (4, 1),  -- accountant -> Tòa A (để xem báo cáo)
    (4, 2),  -- accountant -> Tòa B
    (5, 1);  -- resident -> Tòa A
GO

-- ============================================================================
-- SEED DATA: LOẠI PHÍ CHUNG CỦA MANAGER (toaNha = NULL)
-- ============================================================================
-- Manager 1 (ID=2) - Phí chung áp dụng cho tất cả tòa của manager
INSERT INTO LoaiPhi (TenLoaiPhi, DonGia, DonViTinh, LoaiThu, MoTa, DangHoatDong, ID_NguoiQuanLy, ID_ToaNha)
VALUES 
    -- Phí bắt buộc
    (N'Phí quản lý', 20000, N'VNĐ/m2', 'BatBuoc', N'Phí quản lý chung cư hàng tháng', 1, 2, NULL),
    (N'Phí điện', 3500, N'VNĐ/kWh', 'BatBuoc', N'Phí điện sinh hoạt', 1, 2, NULL),
    (N'Phí nước', 15000, N'VNĐ/m3', 'BatBuoc', N'Phí nước sinh hoạt', 1, 2, NULL),
    (N'Phí vệ sinh', 50000, N'VNĐ/căn', 'BatBuoc', N'Phí vệ sinh môi trường', 1, 2, NULL),
    -- Phí tự nguyện
    (N'Phí giữ xe máy', 100000, N'VNĐ/xe/tháng', 'TuNguyen', N'Phí gửi xe máy', 1, 2, NULL),
    (N'Phí giữ ô tô', 1500000, N'VNĐ/xe/tháng', 'TuNguyen', N'Phí gửi ô tô', 1, 2, NULL),
    (N'Phí internet', 200000, N'VNĐ/tháng', 'TuNguyen', N'Phí internet chung cư', 1, 2, NULL);
GO

-- Manager 2 (ID=3) - Phí chung riêng của manager2
INSERT INTO LoaiPhi (TenLoaiPhi, DonGia, DonViTinh, LoaiThu, MoTa, DangHoatDong, ID_NguoiQuanLy, ID_ToaNha)
VALUES 
    -- Phí bắt buộc
    (N'Phí quản lý', 18000, N'VNĐ/m2', 'BatBuoc', N'Phí quản lý chung cư', 1, 3, NULL),
    (N'Phí điện', 3800, N'VNĐ/kWh', 'BatBuoc', N'Phí điện theo giá EVN', 1, 3, NULL),
    (N'Phí nước', 12000, N'VNĐ/m3', 'BatBuoc', N'Phí nước sinh hoạt', 1, 3, NULL),
    (N'Phí vệ sinh', 40000, N'VNĐ/căn', 'BatBuoc', N'Phí vệ sinh', 1, 3, NULL),
    -- Phí tự nguyện
    (N'Phí gửi xe máy', 80000, N'VNĐ/xe/tháng', 'TuNguyen', N'Phí gửi xe máy', 1, 3, NULL),
    (N'Phí gửi ô tô', 1200000, N'VNĐ/xe/tháng', 'TuNguyen', N'Phí gửi ô tô', 1, 3, NULL);
GO

-- ============================================================================
-- SEED DATA: HỘ GIA ĐÌNH MẪU
-- ============================================================================
INSERT INTO HoGiaDinh (ID_ToaNha, MaHoGiaDinh, TenChuHo, SoDienThoaiLienHe, EmailLienHe, SoTang, SoCanHo, DienTich, TrangThai)
VALUES 
    -- Tòa A
    (1, 'A101', N'Nguyễn Văn An', '0901234567', 'an.nguyen@email.com', 1, '101', 75.5, N'Đang ở'),
    (1, 'A102', N'Trần Thị Bình', '0912345678', 'binh.tran@email.com', 1, '102', 85.0, N'Đang ở'),
    (1, 'A201', N'Lê Văn Cường', '0923456789', 'cuong.le@email.com', 2, '201', 90.0, N'Đang ở'),
    (1, 'A202', N'Phạm Thị Dung', '0934567890', 'dung.pham@email.com', 2, '202', 65.0, N'Đang ở'),
    -- Tòa B  
    (2, 'B101', N'Hoàng Văn Em', '0945678901', 'em.hoang@email.com', 1, '101', 70.0, N'Đang ở'),
    (2, 'B102', N'Vũ Thị Phượng', '0956789012', 'phuong.vu@email.com', 1, '102', 80.0, N'Đang ở');
GO

-- ============================================================================
-- SEED DATA: NHÂN KHẨU MẪU
-- ============================================================================
INSERT INTO NhanKhau (ID_HoGiaDinh, HoTen, NgaySinh, GioiTinh, SoCCCD, SoDienThoai, Email, QuanHeVoiChuHo, LaChuHo, NgayChuyenDen, TrangThai)
VALUES 
    -- Hộ A101
    (1, N'Nguyễn Văn An', '1980-05-15', N'Nam', '001080012345', '0901234567', 'an.nguyen@email.com', N'Chủ hộ', 1, '2020-01-01', N'Thường trú'),
    (1, N'Nguyễn Thị Lan', '1982-08-20', N'Nữ', '001082023456', '0901234568', NULL, N'Vợ', 0, '2020-01-01', N'Thường trú'),
    (1, N'Nguyễn Văn Minh', '2010-03-10', N'Nam', '001010034567', NULL, NULL, N'Con', 0, '2020-01-01', N'Thường trú'),
    -- Hộ A102
    (2, N'Trần Thị Bình', '1975-12-01', N'Nữ', '001075045678', '0912345678', 'binh.tran@email.com', N'Chủ hộ', 1, '2019-06-15', N'Thường trú'),
    -- Hộ B101
    (5, N'Hoàng Văn Em', '1990-07-25', N'Nam', '001090056789', '0945678901', 'em.hoang@email.com', N'Chủ hộ', 1, '2021-03-20', N'Thường trú'),
    (5, N'Hoàng Thị Ngọc', '1992-11-30', N'Nữ', '001092067890', '0945678902', NULL, N'Vợ', 0, '2021-03-20', N'Thường trú');
GO

-- ============================================================================
-- SEED DATA: BẢNG GIÁ RIÊNG CHO TỪNG TÒA (BangGiaDichVu)
-- ============================================================================
-- Tòa A có giá riêng khác với giá mặc định của Manager
INSERT INTO BangGiaDichVu (ID_LoaiPhi, ID_ToaNha, DonGia, GhiChu)
VALUES 
    (1, 1, 22000, N'Giá riêng cho Tòa A - cao cấp'),  -- Phí quản lý Tòa A: 22k thay vì 20k
    (5, 1, 120000, N'Giá riêng xe máy Tòa A');         -- Phí giữ xe máy Tòa A: 120k thay vì 100k
GO

-- ============================================================================
-- HOÀN TẤT
-- ============================================================================
PRINT N'============================================================';
PRINT N'Database QuanLyChungCuDB đã được tạo thành công!';
PRINT N'Phiên bản: 4.1 (Multi-Tenancy SaaS - Phí chung của Manager)';
PRINT N'============================================================';
PRINT N'';
PRINT N'TÀI KHOẢN MẪU:';
PRINT N'- admin / Admin@123 (Quản trị viên hệ thống - xem tất cả)';
PRINT N'- manager / Manager@123 (Quản lý Tòa A)';
PRINT N'- manager2 / Manager@123 (Quản lý Tòa B)';
PRINT N'- accountant / Accountant@123 (Kế toán)';
PRINT N'- resident / Resident@123 (Cư dân)';
PRINT N'============================================================';
PRINT N'';
PRINT N'MULTI-TENANCY SaaS (v4.1):';
PRINT N'- Mỗi Manager có danh sách LOẠI PHÍ CHUNG riêng (toaNha = NULL)';
PRINT N'- Phí CHUNG áp dụng cho TẤT CẢ tòa nhà của Manager đó';
PRINT N'- Phí RIÊNG chỉ áp dụng cho 1 tòa cụ thể (toaNha != NULL)';
PRINT N'- Giá riêng cho từng tòa qua bảng BangGiaDichVu';
PRINT N'- Ưu tiên giá: BangGiaDichVu > LoaiPhi.DonGia';
PRINT N'- Manager A KHÔNG thể xem dữ liệu của Manager B';
PRINT N'============================================================';
PRINT N'';
PRINT N'DỮ LIỆU MẪU:';
PRINT N'- 2 Tòa nhà: Tòa A (manager), Tòa B (manager2)';
PRINT N'- 6 Hộ gia đình: A101, A102, A201, A202, B101, B102';
PRINT N'- 6 Nhân khẩu mẫu';
PRINT N'- Manager 1: 7 loại phí chung';
PRINT N'- Manager 2: 6 loại phí chung';
PRINT N'- Bảng giá riêng cho Tòa A: Phí quản lý 22k, Xe máy 120k';
PRINT N'============================================================';
GO

-- Kiểm tra dữ liệu
SELECT 'Users' AS [Table], COUNT(*) AS [Count] FROM Users
UNION ALL SELECT 'ToaNha', COUNT(*) FROM ToaNha
UNION ALL SELECT 'HoGiaDinh', COUNT(*) FROM HoGiaDinh
UNION ALL SELECT 'NhanKhau', COUNT(*) FROM NhanKhau
UNION ALL SELECT 'LoaiPhi', COUNT(*) FROM LoaiPhi
UNION ALL SELECT 'BangGiaDichVu', COUNT(*) FROM BangGiaDichVu;
GO

alter table ChiSoDienNuoc
add ChiSoCu INT NULL;