-- Active: 1762183366540@@127.0.0.1@1433@QuanLyChungCuDB
USE master;
GO

-- 1. SETUP DATABASE
-- =============================================
DECLARE @DatabaseName nvarchar(50) = N'QuanLyChungCuDB';

IF EXISTS (SELECT name FROM sys.databases WHERE name = @DatabaseName)
BEGIN
    DECLARE @SQL nvarchar(max);
    SET @SQL = N'ALTER DATABASE ' + @DatabaseName + N' SET SINGLE_USER WITH ROLLBACK IMMEDIATE;';
    EXEC(@SQL);
    SET @SQL = N'DROP DATABASE ' + @DatabaseName + N';';
    EXEC(@SQL);
    PRINT 'Database ' + @DatabaseName + ' has been dropped successfully.';
END

CREATE DATABASE QuanLyChungCuDB;
GO

USE QuanLyChungCuDB;
GO

-- 2. CREATE TABLES
-- =============================================

-- Bảng Tòa nhà (Tạo trước để Hộ gia đình tham chiếu)
CREATE TABLE ToaNha (
    ID_ToaNha INT IDENTITY(1,1) PRIMARY KEY,
    TenToaNha NVARCHAR(50) NOT NULL, -- Ví dụ: "Tòa A", "CT1"
    MoTa NVARCHAR(255)               -- Ví dụ: "Khu chung cư cao cấp"
);

-- Bảng Hộ Gia Đình
CREATE TABLE HoGiaDinh (
    ID_HoGiaDinh INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_ToaNha INT NOT NULL, -- FK to ToaNha
    MaHoGiaDinh NVARCHAR(50) NOT NULL UNIQUE,
    TenChuHo NVARCHAR(100),
    SoDienThoaiLienHe VARCHAR(15),
    EmailLienHe VARCHAR(100),
    SoTang INT,
    SoCanHo NVARCHAR(50),
    DienTich FLOAT, 
    TrangThai NVARCHAR(50),
    NgayTao DATETIME DEFAULT GETDATE(),
    NgayCapNhat DATETIME,
    CONSTRAINT FK_HoGiaDinh_ToaNha FOREIGN KEY (ID_ToaNha) REFERENCES ToaNha(ID_ToaNha)
);

-- Bảng Users (Tài khoản đăng nhập)
CREATE TABLE Users (
    ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NULL,
    Username NVARCHAR(100) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(200) NOT NULL,
    FullName NVARCHAR(100),
    Email NVARCHAR(150),
    Role NVARCHAR(50) NOT NULL, -- ADMIN, ACCOUNTANT, RESIDENT
    CreatedAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Users_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh)
);

-- Bảng Nhân Khẩu
CREATE TABLE NhanKhau (
    ID_NhanKhau INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    HoTen NVARCHAR(100) NOT NULL,
    NgaySinh DATE,
    GioiTinh NVARCHAR(10),
    SoCCCD VARCHAR(12) NOT NULL UNIQUE,
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    QuanHeVoiChuHo NVARCHAR(50),
    LaChuHo BIT DEFAULT 0,
    NgayChuyenDen DATE,
    TrangThai NVARCHAR(50),
    CONSTRAINT FK_NhanKhau_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh)
);

-- Bảng Tạm Trú (Đã gộp các cột GioiTinh và DiaChiThuongTru)
CREATE TABLE TamTru (
    ID_TamTru INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    HoTen NVARCHAR(100) NOT NULL,
    SoCCCD VARCHAR(12),
    NgaySinh DATE,
    GioiTinh NVARCHAR(10),       -- Cột mới thêm
    SoDienThoai VARCHAR(15),
    DiaChiThuongTru NVARCHAR(200), -- Cột mới thêm
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    LyDo NVARCHAR(255),
    NgayDangKy DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_TamTru_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh)
);

-- Bảng Tạm Vắng
CREATE TABLE TamVang (
    ID_TamVang INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_NhanKhau INT NOT NULL, 
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    NoiDen NVARCHAR(255),
    LyDo NVARCHAR(255),
    NgayDangKy DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_TamVang_NhanKhau FOREIGN KEY (ID_NhanKhau) REFERENCES NhanKhau(ID_NhanKhau)
);

-- Bảng Loại Phí
CREATE TABLE LoaiPhi (
    ID_LoaiPhi INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TenLoaiPhi NVARCHAR(100) NOT NULL, 
    DonGia DECIMAL(18, 0) DEFAULT 0, 
    DonViTinh NVARCHAR(50), -- m2, người, hộ, xe
    LoaiThu NVARCHAR(50) NOT NULL, -- 'BatBuoc' hoặc 'TuNguyen'
    MoTa NVARCHAR(255),
    DangHoatDong BIT DEFAULT 1
);

-- Bảng Đợt Thu
CREATE TABLE DotThu (
    ID_DotThu INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TenDotThu NVARCHAR(100) NOT NULL, 
    LoaiDotThu NVARCHAR(50), -- 'PhiSinhHoat' hoặc 'DongGop'
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    NgayTao DATETIME DEFAULT GETDATE()
);

-- Bảng Định Mức Thu
CREATE TABLE DinhMucThu (
    ID_DinhMuc INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    ID_LoaiPhi INT NOT NULL,
    SoLuong FLOAT DEFAULT 1, 
    GhiChu NVARCHAR(255),
    CONSTRAINT FK_DinhMuc_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh),
    CONSTRAINT FK_DinhMuc_LoaiPhi FOREIGN KEY (ID_LoaiPhi) REFERENCES LoaiPhi(ID_LoaiPhi)
);

-- Bảng Hóa Đơn
CREATE TABLE HoaDon (
    ID_HoaDon INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    ID_DotThu INT NOT NULL,
    TongTienPhaiThu DECIMAL(18, 0) DEFAULT 0,
    SoTienDaDong DECIMAL(18, 0) DEFAULT 0,
    TrangThai NVARCHAR(50) DEFAULT N'Chưa đóng', -- Chưa đóng, Đang nợ, Đã đóng
    NgayTao DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_HoaDon_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh),
    CONSTRAINT FK_HoaDon_DotThu FOREIGN KEY (ID_DotThu) REFERENCES DotThu(ID_DotThu)
);

-- Bảng Chi Tiết Hóa Đơn
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

-- Bảng Lịch Sử Thanh Toán (Đã gộp các cột VNPAY)
CREATE TABLE LichSuThanhToan (
    ID_GiaoDich INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoaDon INT NOT NULL,
    SoTien DECIMAL(18, 0) NOT NULL,
    NgayNop DATETIME DEFAULT GETDATE(), 
    HinhThuc NVARCHAR(50), -- Tiền mặt / Chuyển khoản
    NguoiNop NVARCHAR(100),
    GhiChu NVARCHAR(255),
    MaGiaoDichVNPAY VARCHAR(50), -- vnp_TransactionNo
    MaNganHang VARCHAR(20),      -- vnp_BankCode
    MaPhanHoi VARCHAR(10),       -- vnp_ResponseCode
    CONSTRAINT FK_ThanhToan_HoaDon FOREIGN KEY (ID_HoaDon) REFERENCES HoaDon(ID_HoaDon)
);

-- Bảng Phản Ánh
CREATE TABLE PhanAnh (
    ID_PhanAnh INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(MAX) NOT NULL,
    NgayGui DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Chờ xử lý',
    CONSTRAINT FK_PhanAnh_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh)
);

-- Bảng Phản Hồi
CREATE TABLE PhanHoi (
    ID_PhanHoi INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_PhanAnh INT NOT NULL,
    NoiDung NVARCHAR(MAX) NOT NULL,
    NguoiTraLoi NVARCHAR(100), -- Tên Admin/BQT
    NgayTraLoi DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_PhanHoi_PhanAnh FOREIGN KEY (ID_PhanAnh) REFERENCES PhanAnh(ID_PhanAnh)
);

-- Bảng Thông Báo (Đã gộp cột ID_HoGiaDinh và DaXem)
CREATE TABLE ThongBao (
    ID_ThongBao INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(MAX),
    NgayTao DATETIME DEFAULT GETDATE(),
    NguoiTao NVARCHAR(100),
    LoaiThongBao NVARCHAR(50), -- Cảnh báo, Tin tức, Phí
    ID_HoGiaDinh INT NULL,     -- NULL: Gửi chung, Có ID: Gửi riêng
    DaXem BIT DEFAULT 0
);
GO

-- 3. CREATE VIEWS
-- =============================================

-- View 1: Thống kê tình hình thu phí theo Đợt thu
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

-- View 2: Thống kê công nợ từng hộ gia đình
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

-- 4. SEED INITIAL DATA
-- =============================================

-- Cần tạo Tòa nhà trước vì HoGiaDinh yêu cầu ID_ToaNha
INSERT INTO ToaNha (TenToaNha, MoTa) VALUES (N'Tòa A', N'Khu căn hộ cao cấp A');

-- Thêm tài khoản mẫu
INSERT INTO Users (Username, PasswordHash, FullName, Email, Role)
VALUES
('admin', '{noop}Admin@123', 'Quan ly chung cu', 'admin@example.com', 'ADMIN'),
('accountant', '{noop}Accountant@123', 'Ke toan', 'accountant@example.com', 'ACCOUNTANT'),
('resident', '{noop}Resident@123', 'Cu dan', 'resident@example.com', 'RESIDENT');

PRINT 'Database setup completed successfully!';
GO