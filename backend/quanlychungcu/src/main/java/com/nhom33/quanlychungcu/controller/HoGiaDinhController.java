package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.dto.HoGiaDinhRequestDTO;
import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.service.HoGiaDinhService;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ho-gia-dinh")
public class HoGiaDinhController {

    private final HoGiaDinhService service;

    public HoGiaDinhController(HoGiaDinhService service) {
        this.service = service;
    }

    /**
     * Tạo mới hộ gia đình kèm chủ hộ (API mới - khuyến khích sử dụng).
     * 
     * Request Body: HoGiaDinhRequestDTO chứa:
     * - Thông tin hộ gia đình (maHoGiaDinh, idToaNha, soCanHo, soTang, dienTich)
     * - Thông tin chủ hộ (hoTen, soCCCD, ngaySinh, gioiTinh, soDienThoai)
     * 
     * Quy tắc:
     * - Cặp (maHoGiaDinh, idToaNha) phải duy nhất
     * - CCCD của chủ hộ phải chưa tồn tại
     * - Chủ hộ được tự động gán laChuHo=true, trangThai="Đang ở"
     * 
     * POST /api/ho-gia-dinh/with-homeowner
     */
    @PostMapping("/with-homeowner")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HoGiaDinh> createWithHomeowner(@Valid @RequestBody HoGiaDinhRequestDTO dto) {
        HoGiaDinh created = service.createHouseholdWithHomeowner(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Tạo mới hộ gia đình (API legacy - không có chủ hộ).
     * Khuyến khích sử dụng POST /api/ho-gia-dinh/with-homeowner thay thế.
     * POST /api/ho-gia-dinh
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HoGiaDinh> create(@Valid @RequestBody HoGiaDinh hoGiaDinh) {
        HoGiaDinh created = service.create(hoGiaDinh);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật hộ gia đình
     * PUT /api/ho-gia-dinh/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HoGiaDinh> update(
            @PathVariable @NonNull Integer id,
            @Valid @RequestBody HoGiaDinh hoGiaDinh) {
        HoGiaDinh updated = service.update(id, hoGiaDinh);
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa hộ gia đình
     * DELETE /api/ho-gia-dinh/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa hộ gia đình thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy hộ gia đình theo ID (kèm danh sách nhân khẩu)
     * GET /api/ho-gia-dinh/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','RESIDENT')")
    public ResponseEntity<HoGiaDinh> getById(@PathVariable @NonNull Integer id) {
        HoGiaDinh hoGiaDinh = service.getDetail(id);
        return ResponseEntity.ok(hoGiaDinh);
    }

    /**
     * Lấy hộ gia đình theo mã hộ
     * GET /api/ho-gia-dinh/ma/{maHoGiaDinh}
     */
    @GetMapping("/ma/{maHoGiaDinh}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<HoGiaDinh> getByMaHoGiaDinh(@PathVariable String maHoGiaDinh) {
        HoGiaDinh hoGiaDinh = service.getByMaHoGiaDinh(maHoGiaDinh);
        return ResponseEntity.ok(hoGiaDinh);
    }

    /**
     * Lấy danh sách hộ gia đình (có phân trang)
     * GET /api/ho-gia-dinh?page=0&size=10&sort=id,desc
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','RESIDENT')")
    public ResponseEntity<Page<HoGiaDinh>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<HoGiaDinh> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Tìm kiếm hộ gia đình theo tên chủ hộ
     * GET /api/ho-gia-dinh/search/ten-chu-ho?tenChuHo=Nguyen&page=0&size=10
     */
    @GetMapping("/search/ten-chu-ho")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<HoGiaDinh>> searchByTenChuHo(
            @RequestParam(required = false) String tenChuHo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<HoGiaDinh> result = service.searchByTenChuHo(tenChuHo, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Tìm kiếm hộ gia đình theo số căn hộ
     * GET /api/ho-gia-dinh/search/can-ho?soCanHo=101
     */
    @GetMapping("/search/can-ho")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<HoGiaDinh>> searchBySoCanHo(
            @RequestParam String soCanHo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<HoGiaDinh> result = service.searchBySoCanHo(soCanHo, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Tìm kiếm hộ gia đình theo tầng
     * GET /api/ho-gia-dinh/search/tang?soTang=5
     */
    @GetMapping("/search/tang")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<HoGiaDinh>> searchByTang(
            @RequestParam @NonNull Integer soTang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<HoGiaDinh> result = service.searchByTang(soTang, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Tìm kiếm đa điều kiện
     * GET /api/ho-gia-dinh/search?maHo=HO001&tenChuHo=Nguyen&soCanHo=101&trangThai=Hoat dong
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<HoGiaDinh>> search(
            @RequestParam(required = false) String maHo,
            @RequestParam(required = false) String tenChuHo,
            @RequestParam(required = false) String soCanHo,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<HoGiaDinh> result = service.search(maHo, tenChuHo, soCanHo, trangThai, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Đếm số hộ gia đình theo trạng thái
     * GET /api/ho-gia-dinh/count?trangThai=Hoat dong
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Map<String, Long>> countByTrangThai(
            @RequestParam String trangThai) {
        
        long count = service.countByTrangThai(trangThai);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}
