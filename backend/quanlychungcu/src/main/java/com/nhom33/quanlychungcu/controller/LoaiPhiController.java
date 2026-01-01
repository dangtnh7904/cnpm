package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.LoaiPhi;
import com.nhom33.quanlychungcu.service.LoaiPhiService;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loai-phi")
public class LoaiPhiController {

    private final LoaiPhiService service;

    public LoaiPhiController(LoaiPhiService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoaiPhi> create(@Valid @RequestBody LoaiPhi loaiPhi) {
        LoaiPhi created = service.create(loaiPhi);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoaiPhi> update(
            @PathVariable @NonNull Integer id,
            @Valid @RequestBody LoaiPhi loaiPhi) {
        LoaiPhi updated = service.update(id, loaiPhi);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa loại phí thành công");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<LoaiPhi> getById(@PathVariable @NonNull Integer id) {
        LoaiPhi loaiPhi = service.getById(id);
        return ResponseEntity.ok(loaiPhi);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<LoaiPhi>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean dangHoatDong) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LoaiPhi> result;
        
        if (dangHoatDong != null) {
            result = service.findByDangHoatDong(dangHoatDong, pageable);
        } else {
            result = service.findAll(pageable);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<LoaiPhi>> findAllActive() {
        List<LoaiPhi> result = service.findAllActive();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<LoaiPhi>> search(
            @RequestParam(required = false) String tenLoaiPhi,
            @RequestParam(required = false) String loaiThu,
            @RequestParam(required = false) Boolean dangHoatDong,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LoaiPhi> result = service.search(tenLoaiPhi, loaiThu, dangHoatDong, pageable);
        return ResponseEntity.ok(result);
    }
}

