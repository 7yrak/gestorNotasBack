package com.gestornotas.controller;

import com.gestornotas.model.entity.SectionGroup;
import com.gestornotas.service.SectionGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sectionGroups")
public class SectionGroupController {
    private final SectionGroupService service;

    public SectionGroupController(SectionGroupService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SectionGroup>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionGroup> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<SectionGroup> create(@RequestBody SectionGroup sectionGroup) {
        return ResponseEntity.ok(service.save(sectionGroup));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionGroup> update(@PathVariable UUID id, @RequestBody SectionGroup sectionGroup) {
        // En una app real, mapearíamos el ID al objeto antes de guardar
        return ResponseEntity.ok(service.save(sectionGroup));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Buscar grupos de secciones hijos directos de un cuaderno
    @GetMapping("/notebook/{notebookId}")
    public ResponseEntity<List<SectionGroup>> getByNotebook(@PathVariable UUID notebookId) {
        // Si usas el servicio base genérico, puedes implementar el filtro rápido en el repositorio
        return ResponseEntity.ok(service.findAll().stream()
                .filter(sg -> notebookId.equals(sg.getNotebookId()) && !Boolean.TRUE.equals(sg.getIsDeleted()))
                .sorted((a,b) -> Integer.compare(a.getOrderInParent(), b.getOrderInParent()))
                .toList());
    }
}
