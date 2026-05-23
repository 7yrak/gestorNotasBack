package com.gestornotas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "pages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "page_id")
    private UUID pageId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "parent_page_id")
    private UUID parentPageId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "order_in_section", nullable = false)
    private Integer orderInSection = 0;

    @Column(name = "last_modified_by_user_id")
    private UUID lastModifiedByUserId;

    @Column(nullable = false)
    private Integer version = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
