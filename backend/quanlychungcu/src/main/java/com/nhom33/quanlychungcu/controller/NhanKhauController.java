package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.dto.DangKyTamTruDTO;
import com.nhom33.quanlychungcu.dto.DangKyTamVangDTO;
import com.nhom33.quanlychungcu.entity.NhanKhau;
import com.nhom33.quanlychungcu.entity.TamTru;
import com.nhom33.quanlychungcu.entity.TamVang;
import com.nhom33.quanlychungcu.service.NhanKhauService;
import com.nhom33.quanlychungcu.service.TamTruService;
import com.nhom33.quanlychungcu.service.TamVangService;
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

@RestController
@RequestMapping("/api/nhan-khau")
public class NhanKhauController {

    private final NhanKhauService service;
    private final TamVangService tamVangService;
    private final TamTruService tamTruService;

    public NhanKhauController(NhanKhauService service, 
                               TamVangService tamVangService, 
                               TamTruService tamTruService) {
        this.service = service;
        this.tamVangService = tamVangService;
        this.tamTruService = tamTruService;
    }

    /**
     * Đăng ký Tạm vắng cho nhân khẩu.
     * 
     * Quy tắc:
     * - Nhân khẩu phải đang ở trạng thái "Đang ở"/"Thường trú"/"Hoạt động"
     * - Không cho phép nếu đang "Tạm vắng"/"Đã chuyển đi"/"Đã mất"
     * - Sau khi đăng ký, trạng thái nhân khẩu tự động chuyển thành "Tạm vắng"
     * 
     * POST /api/nhan-khau/tam-vang
     */
    @PostMapping("/tam-vang")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TamVang> dangKyTamVang(@Valid @RequestBody DangKyTamVangDTO dto) {
        TamVang result = tamVangService.dangKyTamVang(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Đăng ký Tạm trú cho người ngoài vào hộ gia đình.
     * 
     * Quy tắc:
     * - Hộ gia đình phải tồn tại và không ở trạng thái "Trống"/"Không sử dụng"
     * - Người tạm trú là người từ nơi khác đến (không phải nhân khẩu thường trú)
     * 
     * POST /api/nhan-khau/tam-tru
     */
    @PostMapping("/tam-tru")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TamTru> dangKyTamTru(@Valid @RequestBody DangKyTamTruDTO dto) {
        TamTru result = tamTruService.dangKyTamTru(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Tạo mới nhân khẩu (với hoGiaDinhId từ RequestParam)
     * POST /api/nhan-khau?hoGiaDinhId=1
     * Sử dụng logic "Tự động làm Chủ hộ" nếu hộ chưa có ai
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NhanKhau> create(
            @Valid @RequestBody NhanKhau nhanKhau,
            @RequestParam Integer hoGiaDinhId) {
        NhanKhau created = service.addNhanKhau(nhanKhau, hoGiaDinhId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật nhân khẩu
     * PUT /api/nhan-khau/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NhanKhau> update(
            @PathVariable @NonNull Integer id,
            @Valid @RequestBody NhanKhau nhanKhau) {
        NhanKhau updated = service.update(id, nhanKhau);
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
