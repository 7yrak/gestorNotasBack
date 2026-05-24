package com.gestornotas.service;

import com.gestornotas.model.entity.Page;
import com.gestornotas.repository.PageRepository;
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

    public Page findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Page no encontrado"));
    }

    public Page save(Page page) {
        return repository.save(page);
    }

    public Page update(UUID id, Page pageDetails) {
        // Buscamos la página original para no perder el sectionId
        Page existingPage = findById(id);
        
        existingPage.setTitle(pageDetails.getTitle());
        existingPage.setOrderInSection(pageDetails.getOrderInSection());
        existingPage.setParentPageId(pageDetails.getParentPageId());
        
        return repository.save(existingPage);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
