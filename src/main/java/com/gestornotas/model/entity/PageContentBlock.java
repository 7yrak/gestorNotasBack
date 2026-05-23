package com.gestornotas.model.entity;

import com.gestornotas.model.enums.ContentBlockType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "page_content_blocks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PageContentBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "content_block_id")
    private UUID contentBlockId;

    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "content_block_type")
    private ContentBlockType type;

    @Type(JsonBinaryType.class)
    @Column(name = "content_data", columnDefinition = "jsonb", nullable = false)
    private String contentData;

    @Column(name = "x_position")
    private Integer xPosition;

    @Column(name = "y_position")
    private Integer yPosition;

    private Integer width;
    private Integer height;

    @Column(name = "z_index")
    private Integer zIndex = 0;

    @Column(name = "order_on_page", nullable = false)
    private Integer orderOnPage = 0;

    @Column(name = "last_modified_by_user_id")
    private UUID lastModifiedByUserId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
