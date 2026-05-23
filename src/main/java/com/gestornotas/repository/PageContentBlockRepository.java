package com.gestornotas.repository;

import com.gestornotas.model.entity.PageContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PageContentBlockRepository extends JpaRepository<PageContentBlock, UUID> {
}
