package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.PhanAnh;
import com.nhom33.quanlychungcu.entity.PhanHoi;
import com.nhom33.quanlychungcu.service.PhanAnhService;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller quản lý Phản ánh.
 * 
 * ENDPOINTS:
 * - POST /api/phan-anh : User gửi phản ánh (cho tòa nhà mình thuộc)
 * - GET /api/phan-anh : Lấy danh sách phản ánh (theo role)
 * - GET /api/phan-anh/my : User lấy phản ánh của mình
 * - GET /api/phan-anh/toa-nha/{id} : Manager lấy phản ánh của tòa nhà
 * - PUT /api/phan-anh/{id}/trang-thai : Manager cập nhật trạng thái
 * - POST /api/phan-anh/{id}/phan-hoi : Manager phản hồi
 */
@RestController
@RequestMapping("/api/phan-anh")
public class PhanAnhController {

    private final PhanAnhService service;

    public PhanAnhController(PhanAnhService service) {
        this.service = service;
    }

    /**
     * User gửi phản ánh cho tòa nhà mình thuộc.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RESIDENT','ACCOUNTANT')")
    public ResponseEntity<PhanAnh> create(@RequestBody Map<String, Object> request) {
        Integer toaNhaId = (Integer) request.get("toaNhaId");
        String tieuDe = (String) request.get("tieuDe");
        String noiDung = (String) request.get("noiDung");
        
        PhanAnh created = service.create(toaNhaId, tieuDe, noiDung);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Manager cập nhật trạng thái phản ánh.
     */
    @PutMapping("/{id}/trang-thai")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PhanAnh> updateTrangThai(
            @PathVariable @NonNull Integer id,
            @RequestBody Map<String, String> request) {
        String trangThai = request.get("trangThai");
        PhanAnh updated = service.updateTrangThai(id, trangThai);
        return ResponseEntity.ok(updated);
    }

    /**
     * Manager phản hồi phản ánh.
     */
    @PostMapping("/{id}/phan-hoi")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PhanHoi> addPhanHoi(
            @PathVariable @NonNull Integer id,
            @RequestBody Map<String, String> request) {
        PhanHoi phanHoi = service.addPhanHoi(id, request.get("noiDung"), request.get("nguoiTraLoi"));
        return ResponseEntity.status(HttpStatus.CREATED).body(phanHoi);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RESIDENT','ACCOUNTANT')")
    public ResponseEntity<PhanAnh> getById(@PathVariable @NonNull Integer id) {
        PhanAnh phanAnh = service.getById(id);
        return ResponseEntity.ok(phanAnh);
    }

    /**
     * Lấy danh sách phản ánh (theo role).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RESIDENT','ACCOUNTANT')")
    public ResponseEntity<Page<PhanAnh>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PhanAnh> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * User lấy phản ánh của mình.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RESIDENT','ACCOUNTANT')")
    public ResponseEntity<Page<PhanAnh>> findMyPhanAnh(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PhanAnh> result = service.findMyPhanAnh(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Manager lấy phản ánh của tòa nhà mình quản lý.
     */
    @GetMapping("/toa-nha/{toaNhaId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Page<PhanAnh>> findByToaNha(
            @PathVariable @NonNull Integer toaNhaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PhanAnh> result = service.findByToaNha(toaNhaId, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/phan-hoi")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RESIDENT','ACCOUNTANT')")
    public ResponseEntity<List<PhanHoi>> getPhanHoi(@PathVariable @NonNull Integer id) {
        List<PhanHoi> result = service.getPhanHoiByPhanAnh(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Page<PhanAnh>> search(
            @RequestParam(required = false) Integer toaNhaId,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String tieuDe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PhanAnh> result = service.search(toaNhaId, trangThai, tieuDe, pageable);
        return ResponseEntity.ok(result);
    }
}

