package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.dto.ChiSoInputDTO;
import com.nhom33.quanlychungcu.dto.SaveChiSoRequestDTO;
import com.nhom33.quanlychungcu.service.ChiSoDienNuocService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller: API Ghi chỉ số Điện Nước.
 * 
 * Endpoints:
 * - GET /api/chi-so/prepare-input : Lấy danh sách nhập liệu (có chỉ số cũ từ tháng trước)
 * - POST /api/chi-so/save-all     : Lưu chỉ số hàng loạt
 * - GET /api/chi-so/statistics    : Lấy thống kê nhập chỉ số
 */
@RestController
@RequestMapping("/api/chi-so")
@CrossOrigin(origins = "*")
public class ChiSoDienNuocController {

    private final ChiSoDienNuocService service;

    public ChiSoDienNuocController(ChiSoDienNuocService service) {
        this.service = service;
    }

    /**
     * Lấy danh sách các hộ gia đình cần nhập chỉ số.
     * Tự động điền chỉ số cũ từ đợt thu trước.
     * Chỉ lấy các hộ thuộc tòa nhà của đợt thu.
     * 
     * @param dotThuId  ID đợt thu (required)
     * @param loaiPhiId ID loại phí - Điện hoặc Nước (required)
     * @return Danh sách ChiSoInputDTO
     */
    @GetMapping("/prepare-input")
    public ResponseEntity<List<ChiSoInputDTO>> prepareInput(
            @RequestParam Integer dotThuId,
            @RequestParam Integer loaiPhiId) {
        
        List<ChiSoInputDTO> result = service.prepareInput(dotThuId, loaiPhiId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lưu chỉ số hàng loạt và tự động cập nhật hóa đơn.
     * 
     * @param request Request chứa đợt thu, loại phí và danh sách chỉ số
     * @return Thông báo kết quả
     */
    @PostMapping("/save-all")
    public ResponseEntity<Map<String, Object>> saveAll(@Valid @RequestBody SaveChiSoRequestDTO request) {
        int savedCount = service.saveAll(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã lưu " + savedCount + " chỉ số thành công");
        response.put("savedCount", savedCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thống kê nhập chỉ số trong đợt thu.
     * 
     * @param dotThuId  ID đợt thu
     * @param loaiPhiId ID loại phí
     * @return Thống kê (tổng số, đã nhập, chưa nhập, % hoàn thành)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam Integer dotThuId,
            @RequestParam Integer loaiPhiId) {
        
        Map<String, Object> stats = service.getStatistics(dotThuId, loaiPhiId);
        return ResponseEntity.ok(stats);
    }
}
