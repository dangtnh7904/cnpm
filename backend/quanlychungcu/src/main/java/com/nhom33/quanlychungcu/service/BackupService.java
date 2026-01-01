package com.nhom33.quanlychungcu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class BackupService {

    @Value("${app.backup.directory:./backups}")
    private String backupDirectory;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    /**
     * Tạo backup database
     */
    public String createBackup() throws IOException {
        // Tạo thư mục backup nếu chưa có
        Path backupPath = Paths.get(backupDirectory);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
        }

        // Tên file backup
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "backup_" + timestamp + ".sql";
        String backupFilePath = backupDirectory + File.separator + backupFileName;

        // TODO: Thực hiện backup database
        // Có thể sử dụng SQL Server backup command hoặc export data
        // Tạm thời tạo file placeholder
        
        File backupFile = new File(backupFilePath);
        backupFile.createNewFile();
        
        // Ghi thông tin backup vào file
        try (FileOutputStream fos = new FileOutputStream(backupFile)) {
            String info = String.format(
                "-- Backup created at: %s\n" +
                "-- Database: %s\n" +
                "-- This is a placeholder backup file\n" +
                "-- TODO: Implement actual database backup\n",
                LocalDateTime.now(),
                extractDatabaseName(datasourceUrl)
            );
            fos.write(info.getBytes());
        }

        return backupFilePath;
    }

    /**
     * Khôi phục từ backup
     */
    public void restoreBackup(String backupFilePath) throws IOException {
        File backupFile = new File(backupFilePath);
        if (!backupFile.exists()) {
            throw new IOException("File backup không tồn tại: " + backupFilePath);
        }

        // TODO: Thực hiện restore database
        // Có thể sử dụng SQL Server restore command hoặc import data
        System.out.println("Restore từ file: " + backupFilePath);
    }

    /**
     * Lấy danh sách các file backup
     */
    public List<String> listBackups() {
        List<String> backups = new ArrayList<>();
        Path backupPath = Paths.get(backupDirectory);
        
        if (Files.exists(backupPath)) {
            try {
                Files.list(backupPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .forEach(path -> backups.add(path.getFileName().toString()));
            } catch (IOException e) {
                throw new RuntimeException("Lỗi đọc danh sách backup", e);
            }
        }
        
        return backups;
    }

    /**
     * Xóa file backup cũ
     */
    public void deleteBackup(String fileName) throws IOException {
        Path backupPath = Paths.get(backupDirectory, fileName);
        if (Files.exists(backupPath)) {
            Files.delete(backupPath);
        } else {
            throw new IOException("File backup không tồn tại: " + fileName);
        }
    }

    /**
     * Tạo backup và nén thành ZIP
     */
    public String createBackupZip() throws IOException {
        String backupFile = createBackup();
        String zipFile = backupFile.replace(".sql", ".zip");
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
             FileInputStream fis = new FileInputStream(backupFile)) {
            
            ZipEntry entry = new ZipEntry(new File(backupFile).getName());
            zos.putNextEntry(entry);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            
            zos.closeEntry();
        }
        
        // Xóa file SQL gốc
        Files.delete(Paths.get(backupFile));
        
        return zipFile;
    }

    private String extractDatabaseName(String url) {
        if (url == null || url.isEmpty()) {
            return "Unknown";
        }
        int dbNameIndex = url.indexOf("databaseName=");
        if (dbNameIndex > 0) {
            String dbNamePart = url.substring(dbNameIndex + "databaseName=".length());
            int semicolonIndex = dbNamePart.indexOf(";");
            if (semicolonIndex > 0) {
                return dbNamePart.substring(0, semicolonIndex);
            }
            return dbNamePart;
        }
        return "Unknown";
    }
}

