package com.gestornotas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "notebooks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notebook {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notebook_id")
    private UUID notebookId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "order_in_user", nullable = false)
    private Integer orderInUser = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
