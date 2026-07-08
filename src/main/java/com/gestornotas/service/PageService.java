package com.gestornotas.service;

import com.gestornotas.model.entity.Page;
import com.gestornotas.repository.PageRepository;
import com.gestornotas.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class PageService {
    private final PageRepository repository;

    public PageService(PageRepository repository) {
        this.repository = repository;
    }

    public List<Page> findAll() {
        return repository.findAll();
    }

    public List<Page> findBySectionId(UUID sectionId) {
        return repository.findBySectionIdAndIsDeletedFalseOrderByOrderInSectionAsc(sectionId);
    }

    public Page findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Página no encontrada"));
    }

    public Page save(Page page) {
        return repository.save(page);
    }

    public Page update(UUID id, Page pageDetails) {
        // Buscamos la página original para no perder el sectionId
        Page existingPage = findById(id);
        
        if (pageDetails.getTitle() != null) existingPage.setTitle(pageDetails.getTitle().trim());
        if (pageDetails.getColor() != null && pageDetails.getColor().matches("^#[0-9a-fA-F]{6}$")) {
            existingPage.setColor(pageDetails.getColor());
        }
        if (pageDetails.getOrderInSection() != null) existingPage.setOrderInSection(pageDetails.getOrderInSection());
        if (pageDetails.getParentPageId() != null) existingPage.setParentPageId(pageDetails.getParentPageId());
        if (pageDetails.getLastModifiedByUserId() != null) {
            existingPage.setLastModifiedByUserId(pageDetails.getLastModifiedByUserId());
        }
        existingPage.setVersion(existingPage.getVersion() + 1);
        
        return repository.save(existingPage);
    }

    public void deleteById(UUID id) {
        Page page = findById(id);
        page.setIsDeleted(true);
        repository.save(page);
    }
}
