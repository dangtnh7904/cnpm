package com.nhom33.quanlychungcu.controller;

import com.nhom33.quanlychungcu.service.InvoiceService;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @GetMapping("/{idHoaDon}/html")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<String> getInvoiceHtml(@PathVariable @NonNull Integer idHoaDon) {
        String html = service.generateInvoiceHtml(idHoaDon);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
            .body(html);
    }

    @GetMapping("/{idHoaDon}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable @NonNull Integer idHoaDon) throws IOException {
        byte[] pdf = service.generateInvoicePdf(idHoaDon);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hoa-don-" + idHoaDon + ".pdf")
            .body(pdf);
    }
}

