package com.gestornotas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "sections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "notebook_id", nullable = false)
    @NotNull(message = "El cuaderno es obligatorio")
    private UUID notebookId;

    @Column(name = "section_group_id")
    private UUID sectionGroupId;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
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
