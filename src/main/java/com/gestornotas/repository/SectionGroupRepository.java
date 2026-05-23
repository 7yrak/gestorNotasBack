package com.gestornotas.repository;

import com.gestornotas.model.entity.SectionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SectionGroupRepository extends JpaRepository<SectionGroup, UUID> {
}
