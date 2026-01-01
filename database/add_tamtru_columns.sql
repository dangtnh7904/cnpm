-- Active: 1762183366540@@127.0.0.1@1433@QuanLyChungCuDB
-- ============================================
-- Script: Thêm các cột mới cho bảng TamTru
-- Date: 2024
-- Description: Thêm GioiTinh và DiaChiThuongTru
-- ============================================

USE QuanLyChungCu;
GO

-- Thêm cột GioiTinh nếu chưa tồn tại
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'TamTru') AND name = 'GioiTinh')
BEGIN
    ALTER TABLE TamTru ADD GioiTinh NVARCHAR(10) NULL;
    PRINT N'Đã thêm cột GioiTinh vào bảng TamTru';
END
ELSE
BEGIN
    PRINT N'Cột GioiTinh đã tồn tại trong bảng TamTru';
END
GO

-- Thêm cột DiaChiThuongTru nếu chưa tồn tại
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'TamTru') AND name = 'DiaChiThuongTru')
BEGIN
    ALTER TABLE TamTru ADD DiaChiThuongTru NVARCHAR(200) NULL;
    PRINT N'Đã thêm cột DiaChiThuongTru vào bảng TamTru';
END
ELSE
BEGIN
    PRINT N'Cột DiaChiThuongTru đã tồn tại trong bảng TamTru';
END
GO

PRINT N'Hoàn tất cập nhật cấu trúc bảng TamTru!';
GO
