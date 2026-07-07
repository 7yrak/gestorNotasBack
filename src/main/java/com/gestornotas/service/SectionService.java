package com.gestornotas.service;

import com.gestornotas.model.entity.Section;
import com.gestornotas.repository.SectionRepository;
import com.gestornotas.exception.ResourceNotFoundException;
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

    public List<Section> findByNotebookId(UUID notebookId) {
        return repository.findByNotebookIdAndIsDeletedFalseOrderByOrderInParentAsc(notebookId);
    }

    public Section findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sección no encontrada"));
    }

    public Section save(Section section) {
        return repository.save(section);
    }

    public Section update(UUID id, Section sectionDetails) {
        // Buscamos la sección original para no perder datos clave como el notebookId
        Section existingSection = findById(id);
        
        if (sectionDetails.getName() != null) existingSection.setName(sectionDetails.getName().trim());
        if (sectionDetails.getColor() != null) existingSection.setColor(sectionDetails.getColor());
        if (sectionDetails.getOrderInParent() != null) existingSection.setOrderInParent(sectionDetails.getOrderInParent());
        if (sectionDetails.getSectionGroupId() != null) existingSection.setSectionGroupId(sectionDetails.getSectionGroupId());
        
        return repository.save(existingSection);
    }

    public void deleteById(UUID id) {
        Section section = findById(id);
        section.setIsDeleted(true);
        repository.save(section);
    }
}
