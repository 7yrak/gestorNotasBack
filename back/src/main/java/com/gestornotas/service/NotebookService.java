package com.gestornotas.service;

import com.gestornotas.model.entity.Notebook;
import com.gestornotas.repository.NotebookRepository;
import com.gestornotas.exception.ResourceNotFoundException;
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
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cuaderno no encontrado"));
    }

    public Notebook save(Notebook notebook) {
        return repository.save(notebook);
    }

    public Notebook update(UUID id, Notebook notebookDetails) {
        Notebook existingNotebook = findById(id);
        
        if (notebookDetails.getName() != null) existingNotebook.setName(notebookDetails.getName().trim());
        if (notebookDetails.getColor() != null) existingNotebook.setColor(notebookDetails.getColor());
        if (notebookDetails.getOrderInUser() != null) existingNotebook.setOrderInUser(notebookDetails.getOrderInUser());
        
        return repository.save(existingNotebook);
    }

    public void deleteById(UUID id) {
        Notebook notebook = findById(id);
        notebook.setIsDeleted(true);
        repository.save(notebook);
    }
}
