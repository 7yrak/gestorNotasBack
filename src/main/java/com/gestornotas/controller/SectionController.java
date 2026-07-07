package com.gestornotas.controller;

import com.gestornotas.model.entity.Section;
import com.gestornotas.service.SectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sections")
public class SectionController {
    private final SectionService service;

    public SectionController(SectionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Section>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Section> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Section> create(@Valid @RequestBody Section section) {
        return ResponseEntity.ok(service.save(section));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Section> update(@PathVariable UUID id, @RequestBody Section section) {
        return ResponseEntity.ok(service.update(id, section));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    // Obtener secciones raíz de un cuaderno o dentro de un grupo específico
    @GetMapping("/notebook/{notebookId}")
    public ResponseEntity<List<Section>> getByNotebook(@PathVariable UUID notebookId) {
        return ResponseEntity.ok(service.findByNotebookId(notebookId));
    }
}
