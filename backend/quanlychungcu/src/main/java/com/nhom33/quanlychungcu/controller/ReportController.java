package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.service.ReportService;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/dot-thu/{idDotThu}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> getStatisticsByDotThu(@PathVariable @NonNull Integer idDotThu) {
        Map<String, Object> result = service.getStatisticsByDotThu(idDotThu);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}/cong-no")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> getCongNoByHoGiaDinh(@PathVariable @NonNull Integer idHoGiaDinh) {
        Map<String, Object> result = service.getCongNoByHoGiaDinh(idHoGiaDinh);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/thang")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> getStatisticsByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> result = service.getStatisticsByMonth(year, month);
        return ResponseEntity.ok(result);
    }
}

