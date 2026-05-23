package com.gestornotas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "notebook_id", nullable = false)
    private UUID notebookId;

    @Column(name = "section_group_id")
    private UUID sectionGroupId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "order_in_parent", nullable = false)
    private Integer orderInParent = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
