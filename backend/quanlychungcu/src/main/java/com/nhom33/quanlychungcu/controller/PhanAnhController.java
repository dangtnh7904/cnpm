package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.PhanAnh;
import com.nhom33.quanlychungcu.entity.PhanHoi;
import com.nhom33.quanlychungcu.service.PhanAnhService;
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
@RequestMapping("/api/phan-anh")
public class PhanAnhController {

    private final PhanAnhService service;

    public PhanAnhController(PhanAnhService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PhanAnh> create(@Valid @RequestBody PhanAnh phanAnh) {
        PhanAnh created = service.create(phanAnh);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/trang-thai")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhanAnh> updateTrangThai(
            @PathVariable @NonNull Integer id,
            @RequestBody Map<String, String> request) {
        String trangThai = request.get("trangThai");
        PhanAnh updated = service.updateTrangThai(id, trangThai);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/phan-hoi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhanHoi> addPhanHoi(
            @PathVariable @NonNull Integer id,
            @RequestBody Map<String, String> request) {
        PhanHoi phanHoi = service.addPhanHoi(id, request.get("noiDung"), request.get("nguoiTraLoi"));
        return ResponseEntity.status(HttpStatus.CREATED).body(phanHoi);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhanAnh> getById(@PathVariable @NonNull Integer id) {
        PhanAnh phanAnh = service.getById(id);
        return ResponseEntity.ok(phanAnh);
    }

    @GetMapping
    public ResponseEntity<Page<PhanAnh>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PhanAnh> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}")
    public ResponseEntity<Page<PhanAnh>> findByHoGiaDinh(
            @PathVariable @NonNull Integer idHoGiaDinh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PhanAnh> result = service.findByHoGiaDinh(idHoGiaDinh, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/phan-hoi")
    public ResponseEntity<List<PhanHoi>> getPhanHoi(@PathVariable @NonNull Integer id) {
        List<PhanHoi> result = service.getPhanHoiByPhanAnh(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PhanAnh>> search(
            @RequestParam(required = false) Integer idHoGiaDinh,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String tieuDe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PhanAnh> result = service.search(idHoGiaDinh, trangThai, tieuDe, pageable);
        return ResponseEntity.ok(result);
    }
}

