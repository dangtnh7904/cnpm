package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.ToaNha;
import com.nhom33.quanlychungcu.service.ToaNhaService;
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
@RequestMapping("/api/toa-nha")
public class ToaNhaController {

    private final ToaNhaService service;

    public ToaNhaController(ToaNhaService service) {
        this.service = service;
    }

    /**
     * Tạo mới tòa nhà
     * POST /api/toa-nha
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ToaNha> create(@Valid @RequestBody ToaNha toaNha) {
        ToaNha created = service.create(toaNha);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật tòa nhà
     * PUT /api/toa-nha/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ToaNha> update(
            @PathVariable @NonNull Integer id,
            @Valid @RequestBody ToaNha toaNha) {
        ToaNha updated = service.update(id, toaNha);
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa tòa nhà
     * DELETE /api/toa-nha/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa tòa nhà thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tòa nhà theo ID
     * GET /api/toa-nha/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<ToaNha> getById(@PathVariable @NonNull Integer id) {
        ToaNha toaNha = service.getById(id);
        return ResponseEntity.ok(toaNha);
    }

    /**
     * Lấy tất cả tòa nhà (không phân trang - cho dropdown)
     * GET /api/toa-nha/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','RESIDENT')")
    public ResponseEntity<List<ToaNha>> getAll() {
        List<ToaNha> result = service.getAll();
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy danh sách tòa nhà (có phân trang)
     * GET /api/toa-nha?page=0&size=10&sort=id,desc
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<ToaNha>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ToaNha> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Tìm kiếm tòa nhà theo tên
     * GET /api/toa-nha/search?tenToaNha=Toa A
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<Page<ToaNha>> searchByTenToaNha(
            @RequestParam(required = false) String tenToaNha,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ToaNha> result = service.searchByTenToaNha(tenToaNha, pageable);
        return ResponseEntity.ok(result);
    }
}
