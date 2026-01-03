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
 * LOGIC NGHIỆP VỤ MỚI (Tách rời ghi số và thu tiền):
 * - Ghi chỉ số theo Tháng/Năm, không phụ thuộc Đợt thu
 * - Khi tạo Đợt thu có phí Điện/Nước: Query bảng ChiSoDienNuoc để tính tiền
 * 
 * Endpoints:
 * - GET /api/chi-so/prepare-input : Lấy danh sách nhập liệu theo Tháng/Năm/Tòa nhà
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
     * Lấy danh sách các hộ gia đình cần nhập chỉ số cho tháng/năm.
     * Tự động điền chỉ số cũ từ tháng trước.
     * 
     * @param thang     Tháng ghi sổ (1-12)
     * @param nam       Năm ghi sổ
     * @param toaNhaId  ID tòa nhà
     * @param loaiPhiId ID loại phí - Điện hoặc Nước
     * @return Danh sách ChiSoInputDTO
     */
    @GetMapping("/prepare-input")
    public ResponseEntity<List<ChiSoInputDTO>> prepareInput(
            @RequestParam Integer thang,
            @RequestParam Integer nam,
            @RequestParam Integer toaNhaId,
            @RequestParam Integer loaiPhiId) {
        
        List<ChiSoInputDTO> result = service.prepareInput(thang, nam, toaNhaId, loaiPhiId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lưu chỉ số hàng loạt.
     * CHỈ LƯU CHỈ SỐ - KHÔNG TÍNH TIỀN.
     * Việc tính tiền sẽ thực hiện khi tạo Đợt thu có phí Điện/Nước.
     * 
     * @param request Request chứa tháng, năm, tòa nhà, loại phí và danh sách chỉ số
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
     * Lấy thống kê nhập chỉ số trong tháng/năm.
     * 
     * @param thang     Tháng
     * @param nam       Năm
     * @param toaNhaId  ID tòa nhà
     * @param loaiPhiId ID loại phí
     * @return Thống kê (tổng số, đã nhập, chưa nhập, % hoàn thành)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam Integer thang,
            @RequestParam Integer nam,
            @RequestParam Integer toaNhaId,
            @RequestParam Integer loaiPhiId) {
        
        Map<String, Object> stats = service.getStatistics(thang, nam, loaiPhiId, toaNhaId);
        return ResponseEntity.ok(stats);
    }
}
