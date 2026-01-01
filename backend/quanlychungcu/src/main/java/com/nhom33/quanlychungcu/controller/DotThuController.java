package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.DotThu;
import com.nhom33.quanlychungcu.service.DotThuService;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dot-thu")
public class DotThuController {

    private final DotThuService service;

    public DotThuController(DotThuService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DotThu> create(@Valid @RequestBody DotThu dotThu) {
        DotThu created = service.create(dotThu);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DotThu> update(
            @PathVariable @NonNull Integer id,
            @Valid @RequestBody DotThu dotThu) {
        DotThu updated = service.update(id, dotThu);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa đợt thu thành công");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<DotThu> getById(@PathVariable @NonNull Integer id) {
        DotThu dotThu = service.getById(id);
        return ResponseEntity.ok(dotThu);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<DotThu>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DotThu> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<DotThu>> search(
            @RequestParam(required = false) String tenDotThu,
            @RequestParam(required = false) String loaiDotThu,
            @RequestParam(required = false) LocalDate ngayBatDau,
            @RequestParam(required = false) LocalDate ngayKetThuc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DotThu> result = service.search(tenDotThu, loaiDotThu, ngayBatDau, ngayKetThuc, pageable);
        return ResponseEntity.ok(result);
    }
}

