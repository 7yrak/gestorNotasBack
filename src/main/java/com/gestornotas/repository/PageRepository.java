package com.gestornotas.repository;

import com.gestornotas.model.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {
}
