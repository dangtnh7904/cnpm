-- ============================================================================
-- MIGRATION SCRIPT: Refactor bảng TamTru
-- Ngày: 2026-01-01
-- Mô tả: Thay đổi cấu trúc bảng TamTru để liên kết với NhanKhau thay vì HoGiaDinh
-- ============================================================================

USE QuanLyChungCuDB;
GO

-- ============================================================================
-- BƯỚC 1: Backup dữ liệu cũ (nếu có)
-- ============================================================================
IF OBJECT_ID('TamTru_Backup', 'U') IS NOT NULL
    DROP TABLE TamTru_Backup;

IF EXISTS (SELECT 1 FROM TamTru)
BEGIN
    SELECT * INTO TamTru_Backup FROM TamTru;
    PRINT 'Đã backup dữ liệu TamTru cũ vào TamTru_Backup';
END

-- ============================================================================
-- BƯỚC 2: Xóa constraints cũ
-- ============================================================================
-- Xóa foreign key nếu có
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_TamTru_HoGiaDinh')
BEGIN
    ALTER TABLE TamTru DROP CONSTRAINT FK_TamTru_HoGiaDinh;
    PRINT 'Đã xóa FK_TamTru_HoGiaDinh';
END

-- Xóa primary key nếu có
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'PK_TamTru')
BEGIN
    ALTER TABLE TamTru DROP CONSTRAINT PK_TamTru;
    PRINT 'Đã xóa PK_TamTru';
END

-- ============================================================================
-- BƯỚC 3: Xóa bảng TamTru cũ
-- ============================================================================
IF OBJECT_ID('TamTru', 'U') IS NOT NULL
BEGIN
    DROP TABLE TamTru;
    PRINT 'Đã xóa bảng TamTru cũ';
END

-- ============================================================================
-- BƯỚC 4: Tạo bảng TamTru mới (liên kết với NhanKhau)
-- ============================================================================
CREATE TABLE TamTru (
    ID_TamTru INT IDENTITY(1,1) NOT NULL,
    ID_NhanKhau INT NOT NULL,                    -- Thay đổi: Liên kết với NhanKhau
    MaGiayTamTru NVARCHAR(50) NULL,              -- Mới: Mã giấy tạm trú (nếu có)
    DiaChiThuongTru NVARCHAR(200) NULL,          -- Địa chỉ thường trú gốc
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NULL,                       -- Có thể null nếu chưa xác định
    LyDo NVARCHAR(500) NULL,
    NgayDangKy DATETIME DEFAULT GETDATE(),
    
    CONSTRAINT PK_TamTru PRIMARY KEY (ID_TamTru),
    CONSTRAINT FK_TamTru_NhanKhau FOREIGN KEY (ID_NhanKhau) 
        REFERENCES NhanKhau(ID_NhanKhau) ON DELETE CASCADE
);

PRINT 'Đã tạo bảng TamTru mới (liên kết với NhanKhau)';

-- ============================================================================
-- BƯỚC 5: Tạo index cho performance
-- ============================================================================
CREATE INDEX IX_TamTru_NhanKhau ON TamTru(ID_NhanKhau);
CREATE INDEX IX_TamTru_NgayBatDau ON TamTru(NgayBatDau);

PRINT 'Đã tạo các index cho bảng TamTru';

-- ============================================================================
-- THÔNG TIN LOGIC NGHIỆP VỤ MỚI
-- ============================================================================
/*
LOGIC NGHIỆP VỤ ĐĂNG KÝ TẠM TRÚ:

1. Tạm trú = Người từ nơi khác đến ở tạm tại hộ gia đình.

2. Quy trình xử lý (Transaction):
   a) Kiểm tra hộ gia đình tồn tại và không ở trạng thái "Trống".
   b) Kiểm tra CCCD chưa tồn tại trong hệ thống.
   c) INSERT NhanKhau với TrangThai = "Tạm trú".
   d) INSERT TamTru liên kết với NhanKhau vừa tạo.

3. Người tạm trú BẮT BUỘC phải xuất hiện trong danh sách nhân khẩu của hộ.

4. API mới:
   - POST /api/tam-tru/dang-ky  (Đăng ký tạm trú)
   - POST /api/tam-tru/{id}/huy (Hủy tạm trú)
   - GET  /api/tam-tru          (Tra cứu)
*/

GO
