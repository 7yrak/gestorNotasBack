package com.gestornotas.service;

import com.gestornotas.model.entity.Section;
import com.gestornotas.repository.SectionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class SectionService {
    private final SectionRepository repository;

    public SectionService(SectionRepository repository) {
        this.repository = repository;
    }

    public List<Section> findAll() {
        return repository.findAll();
    }

    public Section findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Section no encontrado"));
    }

    public Section save(Section section) {
        return repository.save(section);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
