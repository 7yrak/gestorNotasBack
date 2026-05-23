package com.gestornotas.controller;

import com.gestornotas.model.entity.PageContentBlock;
import com.gestornotas.service.PageContentBlockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pageContentBlocks")
public class PageContentBlockController {
    private final PageContentBlockService service;

    public PageContentBlockController(PageContentBlockService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PageContentBlock>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageContentBlock> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<PageContentBlock> create(@RequestBody PageContentBlock pageContentBlock) {
        return ResponseEntity.ok(service.save(pageContentBlock));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PageContentBlock> update(@PathVariable UUID id, @RequestBody PageContentBlock pageContentBlock) {
        // En una app real, mapearíamos el ID al objeto antes de guardar
        return ResponseEntity.ok(service.save(pageContentBlock));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener los bloques de contenido de una página específica
    @GetMapping("/page/{pageId}")
    public ResponseEntity<List<PageContentBlock>> getByPage(@PathVariable UUID pageId) {
        return ResponseEntity.ok(service.findAll().stream()
                .filter(b -> pageId.equals(b.getPageId()))
                .sorted((a,b) -> Integer.compare(a.getOrderOnPage(), b.getOrderOnPage()))
                .toList());
    }
}
