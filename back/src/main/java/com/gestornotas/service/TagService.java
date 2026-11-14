package com.gestornotas.service;

import com.gestornotas.model.entity.Tag;
import com.gestornotas.repository.TagRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class TagService {
    private final TagRepository repository;

    public TagService(TagRepository repository) {
        this.repository = repository;
    }

    public List<Tag> findAll() {
        return repository.findAll();
    }

    public Tag findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Tag no encontrado"));
    }

    public Tag save(Tag tag) {
        return repository.save(tag);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
