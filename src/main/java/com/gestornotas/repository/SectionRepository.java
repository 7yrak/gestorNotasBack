package com.gestornotas.repository;

import com.gestornotas.model.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByNotebookIdAndIsDeletedFalseOrderByOrderInParentAsc(UUID notebookId);
    List<Section> findByNotebookId(UUID notebookId);
}
