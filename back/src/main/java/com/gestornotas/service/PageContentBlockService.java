package com.gestornotas.service;

import com.gestornotas.model.entity.PageContentBlock;
import com.gestornotas.repository.PageContentBlockRepository;
import com.gestornotas.exception.ResourceNotFoundException;
import com.gestornotas.model.enums.ContentBlockType;
import org.springframework.transaction.annotation.Transactional;
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
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bloque de contenido no encontrado"));
    }

    public List<PageContentBlock> findByPageId(UUID pageId) {
        return repository.findByPageIdOrderByOrderOnPageAsc(pageId);
    }

    public PageContentBlock save(PageContentBlock pageContentBlock) {
        return repository.save(pageContentBlock);
    }

    public PageContentBlock update(UUID id, PageContentBlock details) {
        PageContentBlock block = findById(id);
        block.setType(details.getType());
        block.setContentData(details.getContentData());
        block.setOrderOnPage(details.getOrderOnPage());
        block.setXPosition(details.getXPosition());
        block.setYPosition(details.getYPosition());
        block.setWidth(details.getWidth());
        block.setHeight(details.getHeight());
        block.setZIndex(details.getZIndex());
        block.setLastModifiedByUserId(details.getLastModifiedByUserId());
        return repository.save(block);
    }

    @Transactional
    public PageContentBlock upsertPrimaryText(UUID pageId, PageContentBlock details) {
        PageContentBlock block = repository
                .findFirstByPageIdAndTypeOrderByOrderOnPageAsc(pageId, ContentBlockType.text)
                .orElseGet(PageContentBlock::new);
        block.setPageId(pageId);
        block.setType(ContentBlockType.text);
        block.setContentData(details.getContentData());
        block.setOrderOnPage(0);
        block.setLastModifiedByUserId(details.getLastModifiedByUserId());
        return repository.save(block);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
