

-- Script xóa toàn bộ dữ liệu trừ 3 tài khoản test
-- Chạy script này khi muốn reset database về trạng thái ban đầu

USE QuanLyChungCuDB;
GO

PRINT '========================================';
PRINT 'BẮT ĐẦU XÓA DỮ LIỆU (Giữ lại 3 tài khoản test)';
PRINT '========================================';

-- Xóa theo thứ tự từ bảng con đến bảng cha để tránh FK violation

-- 1. Xóa PhanHoi (con của PhanAnh)
DELETE FROM PhanHoi;
PRINT '✓ Đã xóa bảng PhanHoi';

-- 2. Xóa PhanAnh (con của HoGiaDinh)
DELETE FROM PhanAnh;
PRINT '✓ Đã xóa bảng PhanAnh';

-- 3. Xóa LichSuThanhToan (con của HoaDon)
DELETE FROM LichSuThanhToan;
PRINT '✓ Đã xóa bảng LichSuThanhToan';

-- 4. Xóa ChiTietHoaDon (con của HoaDon)
DELETE FROM ChiTietHoaDon;
PRINT '✓ Đã xóa bảng ChiTietHoaDon';

-- 5. Xóa HoaDon (con của HoGiaDinh và DotThu)
DELETE FROM HoaDon;
PRINT '✓ Đã xóa bảng HoaDon';

-- 6. Xóa DinhMucThu (con của HoGiaDinh và LoaiPhi)
DELETE FROM DinhMucThu;
PRINT '✓ Đã xóa bảng DinhMucThu';

-- 7. Xóa TamVang (con của NhanKhau)
DELETE FROM TamVang;
PRINT '✓ Đã xóa bảng TamVang';

-- 8. Xóa TamTru (con của HoGiaDinh)
DELETE FROM TamTru;
PRINT '✓ Đã xóa bảng TamTru';

-- 9. Xóa NhanKhau (con của HoGiaDinh)
DELETE FROM NhanKhau;
PRINT '✓ Đã xóa bảng NhanKhau';

-- 10. Xóa Users (trừ 3 tài khoản test)
DELETE FROM Users 
WHERE Username NOT IN ('admin', 'accountant', 'resident');
PRINT '✓ Đã xóa bảng Users (giữ lại 3 tài khoản test)';

-- 11. Xóa HoGiaDinh (con của ToaNha)
DELETE FROM HoGiaDinh;
PRINT '✓ Đã xóa bảng HoGiaDinh';

-- 12. Xóa ToaNha
DELETE FROM ToaNha;
PRINT '✓ Đã xóa bảng ToaNha';

-- 13. Xóa DotThu
DELETE FROM DotThu;
PRINT '✓ Đã xóa bảng DotThu';

-- 14. Xóa LoaiPhi
DELETE FROM LoaiPhi;
PRINT '✓ Đã xóa bảng LoaiPhi';

-- 15. Xóa ThongBao
DELETE FROM ThongBao;
PRINT '✓ Đã xóa bảng ThongBao';

-- Reset Identity seeds về 1 (để ID bắt đầu lại từ 1)
DBCC CHECKIDENT ('PhanHoi', RESEED, 0);
DBCC CHECKIDENT ('PhanAnh', RESEED, 0);
DBCC CHECKIDENT ('LichSuThanhToan', RESEED, 0);
DBCC CHECKIDENT ('ChiTietHoaDon', RESEED, 0);
DBCC CHECKIDENT ('HoaDon', RESEED, 0);
DBCC CHECKIDENT ('DinhMucThu', RESEED, 0);
DBCC CHECKIDENT ('TamVang', RESEED, 0);
DBCC CHECKIDENT ('TamTru', RESEED, 0);
DBCC CHECKIDENT ('NhanKhau', RESEED, 0);
DBCC CHECKIDENT ('HoGiaDinh', RESEED, 0);
DBCC CHECKIDENT ('ToaNha', RESEED, 0);
DBCC CHECKIDENT ('DotThu', RESEED, 0);
DBCC CHECKIDENT ('LoaiPhi', RESEED, 0);
DBCC CHECKIDENT ('ThongBao', RESEED, 0);

PRINT '';
PRINT '========================================';
PRINT 'HOÀN THÀNH! Database đã được reset.';
PRINT '========================================';
PRINT '';
PRINT 'Tài khoản còn lại:';
SELECT Username, FullName, Role FROM Users;
