-- Active: 1765413952504@@127.0.0.1@1433@QuanLyChungCuDB


create DATABASE QuanLyChungCuDB;

-- Users & Roles for authentication
CREATE TABLE Users (
    ID INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    Username NVARCHAR(100) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(200) NOT NULL,
    FullName NVARCHAR(100),
    Email NVARCHAR(150),
    Role NVARCHAR(50) NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- Seed default accounts (passwords use Spring Security prefix {noop} for demo)


CREATE TABLE HoGiaDinh (
    ID_HoGiaDinh INT IDENTITY(1,1) NOT NULL,
    MaHoGiaDinh NVARCHAR(50) NOT NULL,
    TenChuHo NVARCHAR(100),
    SoDienThoaiLienHe VARCHAR(15),
    EmailLienHe VARCHAR(100),
    SoTang INT,
    SoCanHo NVARCHAR(50),
    DienTich FLOAT, 
    TrangThai NVARCHAR(50),
    NgayTao DATETIME DEFAULT GETDATE(),
    NgayCapNhat DATETIME
);

CREATE TABLE NhanKhau (
    ID_NhanKhau INT IDENTITY(1,1) NOT NULL,
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
    TrangThai NVARCHAR(50)
);

CREATE TABLE TamTru (
    ID_TamTru INT IDENTITY(1,1) NOT NULL,
    ID_HoGiaDinh INT NOT NULL,
    HoTen NVARCHAR(100) NOT NULL,
    SoCCCD VARCHAR(12),
    NgaySinh DATE,
    SoDienThoai VARCHAR(15),
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    LyDo NVARCHAR(255),
    NgayDangKy DATETIME DEFAULT GETDATE()
);

CREATE TABLE TamVang (
    ID_TamVang INT IDENTITY(1,1) NOT NULL,
    ID_NhanKhau INT NOT NULL, 
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    NoiDen NVARCHAR(255),
    LyDo NVARCHAR(255),
    NgayDangKy DATETIME DEFAULT GETDATE()
);



ALTER TABLE HoGiaDinh
ADD CONSTRAINT PK_HoGiaDinh PRIMARY KEY (ID_HoGiaDinh);

ALTER TABLE NhanKhau
ADD CONSTRAINT PK_NhanKhau PRIMARY KEY (ID_NhanKhau);

ALTER TABLE TamTru
ADD CONSTRAINT PK_TamTru PRIMARY KEY (ID_TamTru);

ALTER TABLE TamVang
ADD CONSTRAINT PK_TamVang PRIMARY KEY (ID_TamVang);

ALTER TABLE HoGiaDinh
ADD CONSTRAINT UC_HoGiaDinh_MaHoGiaDinh UNIQUE (MaHoGiaDinh);

ALTER TABLE NhanKhau
ADD CONSTRAINT UC_NhanKhau_SoCCCD UNIQUE (SoCCCD);

ALTER TABLE NhanKhau
ADD CONSTRAINT FK_NhanKhau_HoGiaDinh
FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh);

ALTER TABLE TamTru
ADD CONSTRAINT FK_TamTru_HoGiaDinh
FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh);

ALTER TABLE TamVang
ADD CONSTRAINT FK_TamVang_NhanKhau
FOREIGN KEY (ID_NhanKhau) REFERENCES NhanKhau(ID_NhanKhau);

-- Bảng định nghĩa các loại phí (Ví dụ: Phí dịch vụ, Phí gửi xe, Quỹ vắc xin)
CREATE TABLE LoaiPhi (
    ID_LoaiPhi INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TenLoaiPhi NVARCHAR(100) NOT NULL, -- [cite: 198] Tên khoản thu
    DonGia DECIMAL(18, 0) DEFAULT 0, -- [cite: 198] Mức thu tiền
    DonViTinh NVARCHAR(50), -- [cite: 198] Đơn vị (m2, người, hộ, xe)
    LoaiThu NVARCHAR(50) NOT NULL, -- [cite: 200] 'BatBuoc' hoặc 'TuNguyen'
    MoTa NVARCHAR(255),
    DangHoatDong BIT DEFAULT 1 -- Để ẩn các khoản thu không còn dùng
);

-- Bảng Đợt thu (Gộp chung cả đợt thu phí tháng và đợt vận động đóng góp)
CREATE TABLE DotThu (
    ID_DotThu INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TenDotThu NVARCHAR(100) NOT NULL, -- Ví dụ: "Thu phí tháng 10/2025", "Ủng hộ bão lụt"
    LoaiDotThu NVARCHAR(50), -- 'PhiSinhHoat' hoặc 'DongGop'
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL, --  Thời hạn thu
    NgayTao DATETIME DEFAULT GETDATE()
);

-- Bảng Định mức thu (Cấu hình số lượng sử dụng cho từng hộ)
-- Bảng này giúp tự động tính toán hóa đơn. VD: Hộ A có 2 xe máy, Hộ B có 85.5m2 diện tích
CREATE TABLE DinhMucThu (
    ID_DinhMuc INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    ID_LoaiPhi INT NOT NULL,
    SoLuong FLOAT DEFAULT 1, -- VD: Diện tích 85.5, hoặc 2 (xe máy)
    GhiChu NVARCHAR(255),
    CONSTRAINT FK_DinhMuc_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh),
    CONSTRAINT FK_DinhMuc_LoaiPhi FOREIGN KEY (ID_LoaiPhi) REFERENCES LoaiPhi(ID_LoaiPhi)
);

-- Bảng Hóa đơn (Tổng hợp công nợ của 1 hộ trong 1 đợt thu)
--  Lưu trạng thái đóng và tổng tiền
CREATE TABLE HoaDon (
    ID_HoaDon INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL,
    ID_DotThu INT NOT NULL,
    TongTienPhaiThu DECIMAL(18, 0) DEFAULT 0, --  Tổng phải thu
    SoTienDaDong DECIMAL(18, 0) DEFAULT 0, -- Số tiền đã đóng thực tế
    TrangThai NVARCHAR(50) DEFAULT N'Chưa đóng', --  Chưa đóng, Đang nợ, Đã đóng
    NgayTao DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_HoaDon_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh),
    CONSTRAINT FK_HoaDon_DotThu FOREIGN KEY (ID_DotThu) REFERENCES DotThu(ID_DotThu)
);

-- Bảng Chi tiết hóa đơn (Lưu chi tiết từng khoản trong hóa đơn đó)
-- Giúp minh bạch phí: Tiền xe bao nhiêu, tiền dịch vụ bao nhiêu
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

-- Bảng Lịch sử thanh toán (Giao dịch nộp tiền)
-- Lưu lịch sử, hình thức chuyển khoản/tiền mặt
CREATE TABLE LichSuThanhToan (
    ID_GiaoDich INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoaDon INT NOT NULL,
    SoTien DECIMAL(18, 0) NOT NULL, -- Số tiền khách thực nộp
    NgayNop DATETIME DEFAULT GETDATE(), 
    HinhThuc NVARCHAR(50), --  Tiền mặt / Chuyển khoản
    NguoiNop NVARCHAR(100), -- Tên người đi nộp (có thể khác chủ hộ)
    GhiChu NVARCHAR(255),
    CONSTRAINT FK_ThanhToan_HoaDon FOREIGN KEY (ID_HoaDon) REFERENCES HoaDon(ID_HoaDon)
);

-- Bảng Phản ánh (Cư dân gửi ý kiến lên BQT)
CREATE TABLE PhanAnh (
    ID_PhanAnh INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_HoGiaDinh INT NOT NULL, -- Người gửi
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(MAX) NOT NULL,
    NgayGui DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Chờ xử lý', -- Chờ xử lý, Đang xử lý, Đã xong
    CONSTRAINT FK_PhanAnh_HoGiaDinh FOREIGN KEY (ID_HoGiaDinh) REFERENCES HoGiaDinh(ID_HoGiaDinh)
);

-- Bảng Phản hồi (BQT trả lời phản ánh)
CREATE TABLE PhanHoi (
    ID_PhanHoi INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ID_PhanAnh INT NOT NULL, -- Trả lời cho phản ánh nào
    NoiDung NVARCHAR(MAX) NOT NULL,
    NguoiTraLoi NVARCHAR(100), -- Tên Admin/BQT
    NgayTraLoi DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_PhanHoi_PhanAnh FOREIGN KEY (ID_PhanAnh) REFERENCES PhanAnh(ID_PhanAnh)
);

-- Bảng Thông báo (Hệ thống/BQT gửi cho cư dân)
CREATE TABLE ThongBao (
    ID_ThongBao INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(MAX),
    NgayTao DATETIME DEFAULT GETDATE(),
    NguoiTao NVARCHAR(100), -- Admin
    LoaiThongBao NVARCHAR(50) --  Cảnh báo, Tin tức, Phí
);


GO
-- View 1: Thống kê tình hình thu phí theo Đợt thu
--  Tổng phải thu, Tiến độ (%), Tổng tiền đã thu
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
--  Xem trạng thái nộp, số tiền nợ của từng hộ
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

INSERT INTO Users (Username, PasswordHash, FullName, Email, Role)
VALUES
('admin', '{noop}Admin@123', 'Quan ly chung cu', 'admin@example.com', 'ADMIN'),
('accountant', '{noop}Accountant@123', 'Ke toan', 'accountant@example.com', 'ACCOUNTANT'),
('resident', '{noop}Resident@123', 'Cu dan', 'resident@example.com', 'RESIDENT')