package com.gestornotas.service;

import com.gestornotas.model.entity.Page;
import com.gestornotas.repository.PageRepository;
import com.gestornotas.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        if (pageDetails.getSectionId() != null) existingPage.setSectionId(pageDetails.getSectionId());
        if (pageDetails.getOrderInSection() != null) existingPage.setOrderInSection(pageDetails.getOrderInSection());
        if (pageDetails.getParentPageId() != null) existingPage.setParentPageId(pageDetails.getParentPageId());
        if (pageDetails.getLastModifiedByUserId() != null) {
            existingPage.setLastModifiedByUserId(pageDetails.getLastModifiedByUserId());
        }
        existingPage.setVersion(existingPage.getVersion() + 1);
        
        return repository.save(existingPage);
    }

    @Transactional
    public Page move(UUID pageId, UUID targetSectionId, int targetIndex) {
        Page page = findById(pageId);
        UUID sourceSectionId = page.getSectionId();
        List<Page> sourcePages = repository.findBySectionIdAndIsDeletedFalseOrderByOrderInSectionAsc(sourceSectionId);
        sourcePages.removeIf(item -> item.getPageId().equals(pageId));

        List<Page> targetPages = sourceSectionId.equals(targetSectionId)
                ? sourcePages
                : repository.findBySectionIdAndIsDeletedFalseOrderByOrderInSectionAsc(targetSectionId);
        targetPages.removeIf(item -> item.getPageId().equals(pageId));

        page.setSectionId(targetSectionId);
        page.setParentPageId(null);
        int safeIndex = Math.max(0, Math.min(targetIndex, targetPages.size()));
        targetPages.add(safeIndex, page);

        for (int index = 0; index < sourcePages.size(); index++) {
            sourcePages.get(index).setOrderInSection(index);
        }
        for (int index = 0; index < targetPages.size(); index++) {
            targetPages.get(index).setOrderInSection(index);
        }

        if (!sourceSectionId.equals(targetSectionId)) repository.saveAll(sourcePages);
        repository.saveAll(targetPages);
        repository.flush();
        return page;
    }

    public void deleteById(UUID id) {
        Page page = findById(id);
        page.setIsDeleted(true);
        repository.save(page);
    }
}
