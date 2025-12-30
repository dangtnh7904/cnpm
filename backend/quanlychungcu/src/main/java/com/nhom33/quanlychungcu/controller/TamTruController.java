package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.entity.TamTru;
import com.nhom33.quanlychungcu.service.TamTruService;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tam-tru")
public class TamTruController {

    private final TamTruService service;

    public TamTruController(TamTruService service) {
        this.service = service;
    }

    @PostMapping
    public TamTru create(@RequestBody TamTru t) {
        return service.create(t);
    }

    @PutMapping("/{id}")
    public TamTru update(@PathVariable @NonNull Integer id, @RequestBody TamTru t) {
        return service.update(id, t);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @NonNull Integer id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    public TamTru getById(@PathVariable @NonNull Integer id) {
        return service.getById(id);
    }

    @GetMapping
    public Page<TamTru> search(
            @RequestParam(required = false) String hoTen,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        return service.searchByName(hoTen, p);
    }
}
