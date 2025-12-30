package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.TamVang;
import com.nhom33.quanlychungcu.service.TamVangService;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tam-vang")
public class TamVangController {

    private final TamVangService service;

    public TamVangController(TamVangService service) {
        this.service = service;
    }

    @PostMapping
    public TamVang create(@RequestBody TamVang t) {
        return service.create(t);
    }

    @PutMapping("/{id}")
    public TamVang update(@PathVariable @NonNull Integer id, @RequestBody TamVang t) {
        return service.update(id, t);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    public TamVang getById(@PathVariable @NonNull Integer id) {
        return service.getById(id);
    }

    @GetMapping
    public Page<TamVang> search(
            @RequestParam(required = false) String noiDen,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        return service.searchByNoiDen(noiDen, p);
    }
}
