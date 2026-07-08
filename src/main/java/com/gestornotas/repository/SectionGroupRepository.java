package com.gestornotas.repository;

import com.gestornotas.model.entity.SectionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface SectionGroupRepository extends JpaRepository<SectionGroup, UUID> {
    List<SectionGroup> findByNotebookId(UUID notebookId);
}
