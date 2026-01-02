-- Active: 1762183366540@@127.0.0.1@1433@QuanLyChungCuDB
-- Active: 1762183366540@@127.0.0.1@1433@master
-- ============================================================================
-- DATABASE SETUP SCRIPT: QuanLyChungCuDB
-- Ngày tạo: 2026-01-01
-- Phiên bản: 2.0 (Refactored với TamTru liên kết NhanKhau)
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
-- PHẦN 1: QUẢN LÝ TÒA NHÀ VÀ HỘ GIA ĐÌNH
-- ============================================================================

-- Bảng Tòa nhà
CREATE TABLE ToaNha (
    ID_ToaNha INT IDENTITY(1,1) PRIMARY KEY,
    TenToaNha NVARCHAR(50) NOT NULL,
    MoTa NVARCHAR(255)
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
    CONSTRAINT UC_HoGiaDinh_MaHoGiaDinh UNIQUE (MaHoGiaDinh)
);

-- ============================================================================
-- PHẦN 2: QUẢN LÝ NHÂN KHẨU
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

-- Bảng Tạm trú (Refactored: Liên kết với NhanKhau)
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

-- Tạo index cho performance
CREATE INDEX IX_TamTru_NhanKhau ON TamTru(ID_NhanKhau);
CREATE INDEX IX_TamTru_NgayBatDau ON TamTru(NgayBatDau);
CREATE INDEX IX_TamVang_NhanKhau ON TamVang(ID_NhanKhau);
CREATE INDEX IX_TamVang_NgayBatDau ON TamVang(NgayBatDau);

-- ============================================================================
-- PHẦN 3: QUẢN LÝ PHÍ VÀ HÓA ĐƠN
-- ============================================================================

-- Bảng Loại phí
CREATE TABLE LoaiPhi (
    ID_LoaiPhi INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TenLoaiPhi NVARCHAR(100) NOT NULL,
    DonGia DECIMAL(18, 0) DEFAULT 0,  -- Giá mặc định (Base Price)
    DonViTinh NVARCHAR(50),
    LoaiThu NVARCHAR(50) NOT NULL,    -- 'Bắt buộc' hoặc 'Tự nguyện'
    MoTa NVARCHAR(255),
    DangHoatDong BIT DEFAULT 1
);

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

-- Bảng Đợt thu
CREATE TABLE DotThu (
    ID_DotThu INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TenDotThu NVARCHAR(100) NOT NULL,
    LoaiDotThu NVARCHAR(50),
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    NgayTao DATETIME DEFAULT GETDATE()
);

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
    CONSTRAINT FK_HoaDon_DotThu FOREIGN KEY (ID_DotThu) REFERENCES DotThu(ID_DotThu)
);

-- Bảng Chi tiết hóa đơn
CREATE TABLE ChiTietHoaDon (
    ID_ChiTiet INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoaDon INT NOT NULL,
    ID_LoaiPhi INT NOT NULL,
    SoLuong FLOAT,
    DonGia DECIMAL(18, 0),
    ThanhTien DECIMAL(18, 0),
    CONSTRAINT FK_ChiTiet_HoaDon FOREIGN KEY (ID_HoaDon) REFERENCES HoaDon(ID_HoaDon),
    CONSTRAINT FK_ChiTiet_LoaiPhi FOREIGN KEY (ID_LoaiPhi) REFERENCES LoaiPhi(ID_LoaiPhi)
);

-- Bảng Lịch sử thanh toán
CREATE TABLE LichSuThanhToan (
    ID_GiaoDich INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoaDon INT NOT NULL,
    SoTien DECIMAL(18, 0) NOT NULL,
    NgayNop DATETIME DEFAULT GETDATE(),
    HinhThuc NVARCHAR(50),
    NguoiNop NVARCHAR(100),
    GhiChu NVARCHAR(255),
    MaGiaoDichVNPAY VARCHAR(50),
    MaNganHang VARCHAR(20),
    MaPhanHoi VARCHAR(10),
    CONSTRAINT FK_ThanhToan_HoaDon FOREIGN KEY (ID_HoaDon) REFERENCES HoaDon(ID_HoaDon)
);

-- ============================================================================
-- PHẦN 4: QUẢN LÝ PHẢN ÁNH VÀ THÔNG BÁO
-- ============================================================================

-- Bảng Phản ánh
CREATE TABLE PhanAnh (
    ID_PhanAnh INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(MAX) NOT NULL,
    NgayGui DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Chờ xử lý',
    CONSTRAINT FK_PhanAnh_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh)
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

-- Bảng Thông báo
CREATE TABLE ThongBao (
    ID_ThongBao INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(MAX),
    NgayTao DATETIME DEFAULT GETDATE(),
    NguoiTao NVARCHAR(100),
    LoaiThongBao NVARCHAR(50),
    ID_HoGiaDinh INT NULL,
    DaXem BIT DEFAULT 0
);

-- ============================================================================
-- PHẦN 5: QUẢN LÝ NGƯỜI DÙNG
-- ============================================================================

-- Bảng Users
CREATE TABLE Users (
    ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NULL,
    Username NVARCHAR(100) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(200) NOT NULL,
    FullName NVARCHAR(100),
    Email NVARCHAR(150),
    Role NVARCHAR(50) NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Users_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh)
);

-- 1. Bảng cấu hình các khoản thu trong Đợt (Giải quyết yêu cầu thêm/bớt phí)
CREATE TABLE ChiSoDienNuoc (
        ID_ChiSo INT IDENTITY(1,1) PRIMARY KEY,
        ID_HoGiaDinh INT NOT NULL,
        ID_DotThu INT NOT NULL,
        ID_LoaiPhi INT NOT NULL,
        ChiSoCu INT DEFAULT 0,
        ChiSoMoi INT NULL,  -- NULL = Chưa nhập chỉ số
        NgayChot DATETIME NULL,
        CONSTRAINT FK_ChiSo_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) 
            REFERENCES HoGiaDinh(ID_HoGiaDinh) ON DELETE CASCADE,
        CONSTRAINT FK_ChiSo_DotThu FOREIGN KEY (ID_DotThu) 
            REFERENCES DotThu(ID_DotThu) ON DELETE CASCADE,
        CONSTRAINT FK_ChiSo_LoaiPhi FOREIGN KEY (ID_LoaiPhi) 
            REFERENCES LoaiPhi(ID_LoaiPhi) ON DELETE CASCADE,
        CONSTRAINT UQ_ChiSo_Ho_DotThu_LoaiPhi UNIQUE (ID_HoGiaDinh, ID_DotThu, ID_LoaiPhi)
    );

-- 2. Bảng lưu chỉ số Điện/Nước (Giải quyết yêu cầu ghi số và báo chưa nhập)
CREATE TABLE DotThu_LoaiPhi (
        ID_Config INT IDENTITY(1,1) PRIMARY KEY,
        ID_DotThu INT NOT NULL,
        ID_LoaiPhi INT NOT NULL,
        CONSTRAINT FK_DotThuLoaiPhi_DotThu FOREIGN KEY (ID_DotThu) 
            REFERENCES DotThu(ID_DotThu) ON DELETE CASCADE,
        CONSTRAINT FK_DotThuLoaiPhi_LoaiPhi FOREIGN KEY (ID_LoaiPhi) 
            REFERENCES LoaiPhi(ID_LoaiPhi) ON DELETE CASCADE,
        CONSTRAINT UQ_DotThu_LoaiPhi UNIQUE (ID_DotThu, ID_LoaiPhi)
    );

    CREATE INDEX IX_ChiSo_HoGiaDinh_LoaiPhi ON ChiSoDienNuoc(ID_HoGiaDinh, ID_LoaiPhi);
    CREATE INDEX IX_ChiSo_DotThu_LoaiPhi ON ChiSoDienNuoc(ID_DotThu, ID_LoaiPhi);
-- ============================================================================
-- PHẦN 6: VIEWS (BÁO CÁO)
-- ============================================================================

-- View: Thống kê tình hình thu phí theo Đợt thu
GO
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
-- PHẦN 7: SEED DATA (DỮ LIỆU MẪU)
-- ============================================================================

-- Seed Users

INSERT INTO LoaiPhi (TenLoaiPhi, DonGia, DonViTinh, LoaiThu, MoTa, DangHoatDong)
    VALUES (N'Điện', 3500, N'kWh', N'BatBuoc', N'Tiền điện sinh hoạt', 1);
INSERT INTO LoaiPhi (TenLoaiPhi, DonGia, DonViTinh, LoaiThu, MoTa, DangHoatDong)
    VALUES (N'Nước', 15000, N'm³', N'BatBuoc', N'Tiền nước sinh hoạt', 1);
INSERT INTO Users (Username, PasswordHash, FullName, Email, Role)
VALUES
    ('admin', '{noop}Admin@123', 'Quan ly chung cu', 'admin@example.com', 'ADMIN'),
    ('accountant', '{noop}Accountant@123', 'Ke toan', 'accountant@example.com', 'ACCOUNTANT'),
    ('resident', '{noop}Resident@123', 'Cu dan', 'resident@example.com', 'RESIDENT');
GO

-- ============================================================================
-- HOÀN TẤT
-- ============================================================================
PRINT '============================================================';
PRINT 'Database QuanLyChungCuDB đã được tạo thành công!';
PRINT 'Phiên bản: 2.0 (Refactored với TamTru liên kết NhanKhau)';
PRINT '============================================================';
PRINT '';
PRINT 'CÁC THAY ĐỔI CHÍNH:';
PRINT '1. Bảng TamTru: Liên kết với NhanKhau (thay vì HoGiaDinh)';
PRINT '2. Thêm field: MaGiayTamTru, DiaChiThuongTru';
PRINT '3. NgayKetThuc: Cho phép NULL (chưa xác định)';
PRINT '4. Cascade DELETE: Khi xóa NhanKhau -> tự động xóa TamTru/TamVang';
PRINT '';
PRINT 'API MỚI:';
PRINT '- POST /api/tam-tru/dang-ky     (Đăng ký tạm trú)';
PRINT '- POST /api/tam-tru/{id}/huy    (Hủy tạm trú)';
PRINT '- POST /api/tam-vang/dang-ky    (Đăng ký tạm vắng)';
PRINT '- POST /api/tam-vang/{id}/ket-thuc (Kết thúc tạm vắng)';
PRINT '============================================================';
GO

select * from hogiadinh;
select * from nhankhau;
