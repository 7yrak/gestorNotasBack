package com.gestornotas.repository;

import com.gestornotas.model.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {
    List<Page> findBySectionIdAndIsDeletedFalseOrderByOrderInSectionAsc(UUID sectionId);
    List<Page> findBySectionId(UUID sectionId);
}
