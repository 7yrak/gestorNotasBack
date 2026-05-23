package com.gestornotas.service;

import com.gestornotas.model.entity.Resource;
import com.gestornotas.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {
    private final ResourceRepository repository;

    public ResourceService(ResourceRepository repository) {
        this.repository = repository;
    }

    public List<Resource> findAll() {
        return repository.findAll();
    }

    public Resource findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Resource no encontrado"));
    }

    public Resource save(Resource resource) {
        return repository.save(resource);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
