package com.gestornotas.controller;

import com.gestornotas.model.entity.Notebook;
import com.gestornotas.service.NotebookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notebooks")
public class NotebookController {
    private final NotebookService service;

    public NotebookController(NotebookService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Notebook>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notebook> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Notebook> create(@RequestBody Notebook notebook) {
        return ResponseEntity.ok(service.save(notebook));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notebook> update(@PathVariable UUID id, @RequestBody Notebook notebook) {
        return ResponseEntity.ok(service.update(id, notebook));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener los cuadernos de un usuario específico
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notebook>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }
}