package com.gestornotas.controller;

import com.gestornotas.model.entity.Page;
import com.gestornotas.service.PageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pages")
public class PageController {
    private final PageService service;

    public PageController(PageService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Page>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Page> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Page> create(@RequestBody Page page) {
        return ResponseEntity.ok(service.save(page));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Page> update(@PathVariable UUID id, @RequestBody Page page) {
        // En una app real, mapearíamos el ID al objeto antes de guardar
        return ResponseEntity.ok(service.save(page));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener las páginas de una sección específica
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<Page>> getBySection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(service.findAll().stream()
                .filter(p -> sectionId.equals(p.getSectionId()) && !Boolean.TRUE.equals(p.getIsDeleted()))
                .sorted((a,b) -> Integer.compare(a.getOrderInSection(), b.getOrderInSection()))
                .toList());
    }
}
