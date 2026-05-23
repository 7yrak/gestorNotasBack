package com.gestornotas.service;

import com.gestornotas.model.entity.Notebook;
import com.gestornotas.repository.NotebookRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class NotebookService {
    private final NotebookRepository repository;

    public NotebookService(NotebookRepository repository) {
        this.repository = repository;
    }

    public List<Notebook> findAll() {
        return repository.findAll();
    }

    public List<Notebook> findByUserId(UUID userId) {
        return repository.findByUserIdAndIsDeletedFalseOrderByOrderInUserAsc(userId);
    }

    public Notebook findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Notebook no encontrado"));
    }

    public Notebook save(Notebook notebook) {
        return repository.save(notebook);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
