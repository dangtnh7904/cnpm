package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.HoaDon;
import com.nhom33.quanlychungcu.entity.LichSuThanhToan;
import com.nhom33.quanlychungcu.service.HoaDonService;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonController {

    private final HoaDonService service;

    public HoaDonController(HoaDonService service) {
        this.service = service;
    }

    @PostMapping("/tao-cho-ho/{idHoGiaDinh}/dot-thu/{idDotThu}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HoaDon> createHoaDon(
            @PathVariable @NonNull Integer idHoGiaDinh,
            @PathVariable @NonNull Integer idDotThu) {
        HoaDon created = service.createHoaDonForHoGiaDinh(idHoGiaDinh, idDotThu);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/trang-thai")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HoaDon> updateTrangThai(
            @PathVariable @NonNull Integer id,
            @RequestBody Map<String, String> request) {
        String trangThai = request.get("trangThai");
        HoaDon updated = service.updateTrangThai(id, trangThai);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/thanh-toan")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<LichSuThanhToan> addPayment(
            @PathVariable @NonNull Integer id,
            @RequestBody Map<String, Object> request) {
        
        BigDecimal soTien = new BigDecimal(request.get("soTien").toString());
        String hinhThuc = (String) request.get("hinhThuc");
        String nguoiNop = (String) request.get("nguoiNop");
        String ghiChu = (String) request.get("ghiChu");
        
        LichSuThanhToan thanhToan = service.addPayment(id, soTien, hinhThuc, nguoiNop, ghiChu);
        return ResponseEntity.status(HttpStatus.CREATED).body(thanhToan);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<HoaDon> getById(@PathVariable @NonNull Integer id) {
        HoaDon hoaDon = service.getById(id);
        return ResponseEntity.ok(hoaDon);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<HoaDon>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<HoaDon> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<HoaDon>> findByHoGiaDinh(
            @PathVariable @NonNull Integer idHoGiaDinh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<HoaDon> result = service.findByHoGiaDinh(idHoGiaDinh, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/lich-su-thanh-toan")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<LichSuThanhToan>> getLichSuThanhToan(@PathVariable @NonNull Integer id) {
        List<LichSuThanhToan> result = service.getLichSuThanhToan(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<HoaDon>> search(
            @RequestParam(required = false) Integer idHoGiaDinh,
            @RequestParam(required = false) Integer idDotThu,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<HoaDon> result = service.search(idHoGiaDinh, idDotThu, trangThai, pageable);
        return ResponseEntity.ok(result);
    }
}

