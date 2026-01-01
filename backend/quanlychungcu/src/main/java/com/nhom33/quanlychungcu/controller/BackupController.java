package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.service.BackupService;
import lombok.NonNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final BackupService service;

    public BackupController(BackupService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createBackup() {
        try {
            String backupPath = service.createBackup();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Tạo backup thành công");
            response.put("backupPath", backupPath);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Lỗi tạo backup: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/create-zip")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> createBackupZip() {
        try {
            String zipPath = service.createBackupZip();
            Resource resource = new FileSystemResource(zipPath);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + new java.io.File(zipPath).getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> restoreBackup(@RequestParam String fileName) {
        try {
            service.restoreBackup(fileName);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Khôi phục backup thành công");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Lỗi khôi phục backup: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listBackups() {
        List<String> backups = service.listBackups();
        return ResponseEntity.ok(backups);
    }

    @DeleteMapping("/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteBackup(@PathVariable @NonNull String fileName) {
        try {
            service.deleteBackup(fileName);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Xóa backup thành công");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Lỗi xóa backup: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

