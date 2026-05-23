package com.gestornotas.service;

import com.gestornotas.model.entity.PageContentBlock;
import com.gestornotas.repository.PageContentBlockRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class PageContentBlockService {
    private final PageContentBlockRepository repository;

    public PageContentBlockService(PageContentBlockRepository repository) {
        this.repository = repository;
    }

    public List<PageContentBlock> findAll() {
        return repository.findAll();
    }

    public PageContentBlock findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("PageContentBlock no encontrado"));
    }

    public PageContentBlock save(PageContentBlock pageContentBlock) {
        return repository.save(pageContentBlock);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
