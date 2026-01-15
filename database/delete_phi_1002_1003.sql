-- Active: 1767516438826@@127.0.0.1@1433@QuanLyChungCuDB
-- ============================================================================
-- XÓA PHÍ THEO ID CỤ THỂ (1002, 1003)
-- ============================================================================

USE QuanLyChungCuDB;
GO

-- Xóa tất cả bảng liên quan (theo thứ tự FK)
DELETE FROM ChiTietHoaDon WHERE ID_LoaiPhi IN (1002, 1003);
DELETE FROM DinhMucThu WHERE ID_LoaiPhi IN (1002, 1003);
DELETE FROM ChiSoDienNuoc WHERE ID_LoaiPhi IN (1002, 1003);
DELETE FROM DotThu_LoaiPhi WHERE ID_LoaiPhi IN (1002, 1003);
DELETE FROM BangGiaDichVu WHERE ID_LoaiPhi IN (1002, 1003);

-- Xóa loại phí
DELETE FROM LoaiPhi WHERE ID_LoaiPhi IN (1002, 1003);

-- Kiểm tra
SELECT * FROM LoaiPhi;
GO
