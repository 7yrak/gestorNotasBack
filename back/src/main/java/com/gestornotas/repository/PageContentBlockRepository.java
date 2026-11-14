package com.gestornotas.repository;

import com.gestornotas.model.entity.PageContentBlock;
import com.gestornotas.model.enums.ContentBlockType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface PageContentBlockRepository extends JpaRepository<PageContentBlock, UUID> {
    List<PageContentBlock> findByPageIdOrderByOrderOnPageAsc(UUID pageId);
    Optional<PageContentBlock> findFirstByPageIdAndTypeOrderByOrderOnPageAsc(UUID pageId, ContentBlockType type);
}
