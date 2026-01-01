package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.DinhMucThu;
import com.nhom33.quanlychungcu.service.DinhMucThuService;
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
@RequestMapping("/api/dinh-muc-thu")
public class DinhMucThuController {

    private final DinhMucThuService service;

    public DinhMucThuController(DinhMucThuService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DinhMucThu> create(@Valid @RequestBody DinhMucThu dinhMuc) {
        DinhMucThu created = service.create(dinhMuc);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DinhMucThu> update(
            @PathVariable @NonNull Integer id,
            @Valid @RequestBody DinhMucThu dinhMuc) {
        DinhMucThu updated = service.update(id, dinhMuc);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa định mức thu thành công");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<DinhMucThu> getById(@PathVariable @NonNull Integer id) {
        DinhMucThu dinhMuc = service.getById(id);
        return ResponseEntity.ok(dinhMuc);
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<DinhMucThu>> findByHoGiaDinh(@PathVariable @NonNull Integer idHoGiaDinh) {
        List<DinhMucThu> result = service.findByHoGiaDinh(idHoGiaDinh);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}/page")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<DinhMucThu>> findByHoGiaDinhPage(
            @PathVariable @NonNull Integer idHoGiaDinh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DinhMucThu> result = service.findByHoGiaDinh(idHoGiaDinh, pageable);
        return ResponseEntity.ok(result);
    }
}

