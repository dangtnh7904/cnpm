package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.dto.NhanKhauRequestDTO;
import com.nhom33.quanlychungcu.entity.NhanKhau;
import com.nhom33.quanlychungcu.service.NhanKhauService;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý API Nhân khẩu.
 * 
 * Lưu ý: API Tạm vắng/Tạm trú đã chuyển sang Controller riêng:
 * - POST /api/tam-vang/dang-ky
 * - POST /api/tam-tru/dang-ky
 */
@RestController
@RequestMapping("/api/nhan-khau")
public class NhanKhauController {

    private final NhanKhauService service;

    public NhanKhauController(NhanKhauService service) {
        this.service = service;
    }

    /**
     * Thêm nhân khẩu vào hộ gia đình (API mới với validation nghiêm ngặt).
     * 
     * LUỒNG NGHIỆP VỤ:
     * - Nếu QuanHeVoiChuHo = "Chủ hộ", kiểm tra hộ đã có chủ hộ chưa.
     * - Nếu có -> Trả về lỗi 400: "Hộ gia đình này đã có chủ hộ. Vui lòng chọn quan hệ khác".
     * - Nếu chưa -> Lưu và tự động cập nhật TenChuHo trong bảng HoGiaDinh.
     * - Cập nhật trạng thái hộ gia đình thành "Đang ở" nếu đang "Trống".
     * 
     * POST /api/nhan-khau
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NhanKhau> create(@Valid @RequestBody NhanKhauRequestDTO dto) {
        NhanKhau created = service.addNhanKhauWithValidation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật nhân khẩu (với validation nghiêm ngặt).
     * 
     * QUY TẮC NGHIÊM NGẶT:
     * - KHÔNG cho phép thay đổi TrangThai qua API này.
     * - Việc thay đổi trạng thái phải thực hiện qua API nghiệp vụ riêng (Tạm vắng/Tạm trú).
     * - Nếu đổi QuanHeVoiChuHo thành "Chủ hộ", kiểm tra hộ đã có chủ hộ khác chưa.
     * 
     * PUT /api/nhan-khau/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NhanKhau> update(
            @PathVariable @NonNull Integer id,
            @Valid @RequestBody NhanKhauRequestDTO dto) {
        NhanKhau updated = service.updateNhanKhauWithValidation(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa nhân khẩu
     * DELETE /api/nhan-khau/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa nhân khẩu thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy nhân khẩu theo ID
     * GET /api/nhan-khau/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NhanKhau> getById(@PathVariable @NonNull Integer id) {
        NhanKhau nhanKhau = service.getById(id);
        return ResponseEntity.ok(nhanKhau);
    }

    /**
     * Lấy nhân khẩu theo số CCCD
     * GET /api/nhan-khau/cccd/{soCCCD}
     */
    @GetMapping("/cccd/{soCCCD}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NhanKhau> getBySoCCCD(@PathVariable String soCCCD) {
        NhanKhau nhanKhau = service.getBySoCCCD(soCCCD);
        return ResponseEntity.ok(nhanKhau);
    }

    /**
     * Lấy danh sách nhân khẩu (có phân trang)
     * GET /api/nhan-khau?page=0&size=10&sort=id,desc
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NhanKhau>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NhanKhau> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy danh sách nhân khẩu theo hộ gia đình
     * GET /api/nhan-khau/ho-gia-dinh/{idHoGiaDinh}
     */
    @GetMapping("/ho-gia-dinh/{idHoGiaDinh}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NhanKhau>> getByHoGiaDinh(@PathVariable @NonNull Integer idHoGiaDinh) {
        List<NhanKhau> result = service.findByHoGiaDinh(idHoGiaDinh);
        return ResponseEntity.ok(result);
    }

    /**
     * Tìm kiếm nhân khẩu theo họ tên
     * GET /api/nhan-khau/search/ho-ten?hoTen=Nguyen&page=0&size=10
     */
    @GetMapping("/search/ho-ten")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NhanKhau>> searchByHoTen(
            @RequestParam(required = false) String hoTen,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NhanKhau> result = service.searchByHoTen(hoTen, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Tìm kiếm đa điều kiện
     * GET /api/nhan-khau/search?hoTen=Nguyen&soCCCD=123456789012&gioiTinh=Nam
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NhanKhau>> search(
            @RequestParam(required = false) String hoTen,
            @RequestParam(required = false) String soCCCD,
            @RequestParam(required = false) String gioiTinh,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) Integer idHoGiaDinh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NhanKhau> result = service.search(hoTen, soCCCD, gioiTinh, trangThai, idHoGiaDinh, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Đếm số nhân khẩu trong hộ gia đình
     * GET /api/nhan-khau/count/ho-gia-dinh?id=1
     */
    @GetMapping("/count/ho-gia-dinh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countByHoGiaDinh(
            @RequestParam @NonNull Integer id) {
        
        long count = service.countByHoGiaDinh(id);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Đếm số nhân khẩu theo giới tính
     * GET /api/nhan-khau/count/gioi-tinh?gioiTinh=Nam
     */
    @GetMapping("/count/gioi-tinh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countByGioiTinh(
            @RequestParam String gioiTinh) {
        
        long count = service.countByGioiTinh(gioiTinh);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}
