-- =============================================
-- Migration: Thêm bảng cho module Điện Nước
-- Ngày tạo: 2024
-- =============================================

-- 1. Bảng DotThu_LoaiPhi: Cấu hình loại phí trong mỗi đợt thu
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='DotThu_LoaiPhi' AND xtype='U')
BEGIN
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
    PRINT N'Đã tạo bảng DotThu_LoaiPhi';
END
GO

-- 2. Bảng ChiSoDienNuoc: Lưu chỉ số điện nước của từng hộ
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ChiSoDienNuoc' AND xtype='U')
BEGIN
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
    PRINT N'Đã tạo bảng ChiSoDienNuoc';
END
GO

-- 3. Tạo index cho tìm kiếm nhanh
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_ChiSo_DotThu_LoaiPhi')
BEGIN
    CREATE INDEX IX_ChiSo_DotThu_LoaiPhi ON ChiSoDienNuoc(ID_DotThu, ID_LoaiPhi);
    PRINT N'Đã tạo index IX_ChiSo_DotThu_LoaiPhi';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_ChiSo_HoGiaDinh_LoaiPhi')
BEGIN
    CREATE INDEX IX_ChiSo_HoGiaDinh_LoaiPhi ON ChiSoDienNuoc(ID_HoGiaDinh, ID_LoaiPhi);
    PRINT N'Đã tạo index IX_ChiSo_HoGiaDinh_LoaiPhi';
END
GO

-- 4. Thêm dữ liệu mẫu cho loại phí Điện và Nước nếu chưa có
IF NOT EXISTS (SELECT 1 FROM LoaiPhi WHERE TenLoaiPhi = N'Điện')
BEGIN
    INSERT INTO LoaiPhi (TenLoaiPhi, DonGia, DonViTinh, LoaiThu, MoTa, DangHoatDong)
    VALUES (N'Điện', 3500, N'kWh', N'BatBuoc', N'Tiền điện sinh hoạt', 1);
    PRINT N'Đã thêm loại phí Điện';
END
GO

IF NOT EXISTS (SELECT 1 FROM LoaiPhi WHERE TenLoaiPhi = N'Nước')
BEGIN
    INSERT INTO LoaiPhi (TenLoaiPhi, DonGia, DonViTinh, LoaiThu, MoTa, DangHoatDong)
    VALUES (N'Nước', 15000, N'm³', N'BatBuoc', N'Tiền nước sinh hoạt', 1);
    PRINT N'Đã thêm loại phí Nước';
END
GO

PRINT N'Migration hoàn tất!';
