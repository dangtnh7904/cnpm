-- ============================================================
-- MIGRATION: Thêm cột ID_ToaNha vào bảng DotThu
-- Mỗi đợt thu thuộc về một tòa nhà cụ thể
-- Cho phép cùng tên đợt thu ở các tòa khác nhau
-- ============================================================

-- Kiểm tra và thêm cột ID_ToaNha nếu chưa tồn tại
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'DotThu' AND COLUMN_NAME = 'ID_ToaNha'
)
BEGIN
    ALTER TABLE DotThu 
    ADD ID_ToaNha INT NULL;
    
    PRINT N'Đã thêm cột ID_ToaNha vào bảng DotThu';
END
ELSE
BEGIN
    PRINT N'Cột ID_ToaNha đã tồn tại trong bảng DotThu';
END
GO

-- Thêm Foreign Key constraint
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys 
    WHERE name = 'FK_DotThu_ToaNha'
)
BEGIN
    ALTER TABLE DotThu
    ADD CONSTRAINT FK_DotThu_ToaNha 
    FOREIGN KEY (ID_ToaNha) REFERENCES ToaNha(ID_ToaNha);
    
    PRINT N'Đã thêm Foreign Key FK_DotThu_ToaNha';
END
GO

-- Tạo index để tối ưu query tìm theo tòa nhà
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = 'IX_DotThu_ToaNha' AND object_id = OBJECT_ID('DotThu')
)
BEGIN
    CREATE INDEX IX_DotThu_ToaNha ON DotThu(ID_ToaNha);
    PRINT N'Đã tạo index IX_DotThu_ToaNha';
END
GO

-- Cập nhật đợt thu cũ: Gán tòa nhà mặc định (tòa đầu tiên) cho các đợt thu chưa có tòa
-- (Chỉ chạy nếu có đợt thu NULL và có ít nhất 1 tòa nhà)
DECLARE @DefaultToaNhaId INT;
SELECT TOP 1 @DefaultToaNhaId = ID_ToaNha FROM ToaNha ORDER BY ID_ToaNha;

IF @DefaultToaNhaId IS NOT NULL
BEGIN
    UPDATE DotThu 
    SET ID_ToaNha = @DefaultToaNhaId 
    WHERE ID_ToaNha IS NULL;
    
    PRINT N'Đã gán tòa nhà mặc định cho các đợt thu cũ';
END
GO

-- Thống kê sau migration
SELECT 
    COUNT(*) AS TotalDotThu,
    COUNT(ID_ToaNha) AS DotThuWithToaNha,
    COUNT(*) - COUNT(ID_ToaNha) AS DotThuWithoutToaNha
FROM DotThu;
GO

PRINT N'=== Migration hoàn tất ===';
