package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.TamTru;
import com.nhom33.quanlychungcu.service.TamTruService;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tam-tru")
public class TamTruController {

    private final TamTruService service;

    public TamTruController(TamTruService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TamTru create(@RequestBody TamTru t) {
        return service.create(t);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TamTru update(@PathVariable @NonNull Integer id, @RequestBody TamTru t) {
        return service.update(id, t);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TamTru getById(@PathVariable @NonNull Integer id) {
        return service.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TamTru> search(
            @RequestParam(required = false) String hoTen,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        return service.searchByName(hoTen, p);
    }
}
