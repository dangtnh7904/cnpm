-- Active: 1767516438826@@127.0.0.1@1433@QuanLyChungCuDB
-- ============================================================================
-- XÓA CÁC LOẠI PHÍ TỰ NGUYỆN
-- Chạy script này trên database QuanLyChungCuDB
-- ============================================================================

USE QuanLyChungCuDB;
GO

-- Xem danh sách phí tự nguyện trước khi xóa
SELECT ID_LoaiPhi, TenLoaiPhi, DonGia, LoaiThu, ID_NguoiQuanLy 
FROM LoaiPhi 
WHERE LoaiThu = 'TuNguyen';

-- Xóa tất cả bảng liên quan đến phí tự nguyện (theo thứ tự FK)
-- 1. Chi tiết hóa đơn
DELETE FROM ChiTietHoaDon 
WHERE ID_LoaiPhi IN (SELECT ID_LoaiPhi FROM LoaiPhi WHERE LoaiThu = 'TuNguyen');

-- 2. Định mức thu
DELETE FROM DinhMucThu 
WHERE ID_LoaiPhi IN (SELECT ID_LoaiPhi FROM LoaiPhi WHERE LoaiThu = 'TuNguyen');

-- 3. Chỉ số điện nước
DELETE FROM ChiSoDienNuoc 
WHERE ID_LoaiPhi IN (SELECT ID_LoaiPhi FROM LoaiPhi WHERE LoaiThu = 'TuNguyen');

-- 4. Cấu hình đợt thu - loại phí
DELETE FROM DotThu_LoaiPhi 
WHERE ID_LoaiPhi IN (SELECT ID_LoaiPhi FROM LoaiPhi WHERE LoaiThu = 'TuNguyen');

-- 5. Bảng giá dịch vụ
DELETE FROM BangGiaDichVu 
WHERE ID_LoaiPhi IN (SELECT ID_LoaiPhi FROM LoaiPhi WHERE LoaiThu = 'TuNguyen');

-- 6. Cuối cùng xóa loại phí
DELETE FROM LoaiPhi WHERE LoaiThu = 'TuNguyen';

-- Kiểm tra lại
SELECT * FROM LoaiPhi;

PRINT N'Đã xóa tất cả phí tự nguyện!';
GO
