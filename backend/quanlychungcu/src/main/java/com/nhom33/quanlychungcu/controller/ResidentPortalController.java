package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.HoaDon;
import com.nhom33.quanlychungcu.entity.LichSuThanhToan;
import com.nhom33.quanlychungcu.entity.PhanAnh;
import com.nhom33.quanlychungcu.service.ResidentPortalService;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resident")
public class ResidentPortalController {

    private final ResidentPortalService service;

    public ResidentPortalController(ResidentPortalService service) {
        this.service = service;
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}/lich-su-thanh-toan")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','RESIDENT')")
    public ResponseEntity<Page<HoaDon>> getPaymentHistory(
            @PathVariable @NonNull Integer idHoGiaDinh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<HoaDon> result = service.getPaymentHistory(idHoGiaDinh, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/hoa-don/{idHoaDon}/chi-tiet")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','RESIDENT')")
    public ResponseEntity<List<LichSuThanhToan>> getPaymentDetails(@PathVariable @NonNull Integer idHoaDon) {
        List<LichSuThanhToan> result = service.getPaymentDetails(idHoaDon);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}/cong-no")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','RESIDENT')")
    public ResponseEntity<Map<String, Object>> getCurrentDebt(@PathVariable @NonNull Integer idHoGiaDinh) {
        BigDecimal debt = service.getCurrentDebt(idHoGiaDinh);
        Map<String, Object> result = new HashMap<>();
        result.put("tongCongNo", debt);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}/phan-anh")
    public ResponseEntity<Page<PhanAnh>> getPhanAnh(
            @PathVariable @NonNull Integer idHoGiaDinh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PhanAnh> result = service.getPhanAnhByHoGiaDinh(idHoGiaDinh, pageable);
        return ResponseEntity.ok(result);
    }
}

