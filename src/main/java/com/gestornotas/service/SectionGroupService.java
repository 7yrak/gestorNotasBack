package com.gestornotas.service;

import com.gestornotas.model.entity.SectionGroup;
import com.gestornotas.repository.SectionGroupRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class SectionGroupService {
    private final SectionGroupRepository repository;

    public SectionGroupService(SectionGroupRepository repository) {
        this.repository = repository;
    }

    public List<SectionGroup> findAll() {
        return repository.findAll();
    }

    public SectionGroup findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("SectionGroup no encontrado"));
    }

    public SectionGroup save(SectionGroup sectionGroup) {
        return repository.save(sectionGroup);
    }

    public SectionGroup update(UUID id, SectionGroup sectionGroupDetails) {
        SectionGroup existingSectionGroup = findById(id);
        
        existingSectionGroup.setName(sectionGroupDetails.getName());
        existingSectionGroup.setOrderInParent(sectionGroupDetails.getOrderInParent());
        existingSectionGroup.setParentSectionGroupId(sectionGroupDetails.getParentSectionGroupId());
        
        return repository.save(existingSectionGroup);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
