package com.gestornotas.service;

import com.gestornotas.model.entity.PageContentBlock;
import com.gestornotas.model.enums.ContentBlockType;
import com.gestornotas.repository.PageContentBlockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageContentBlockServiceTest {

    @Mock
    private PageContentBlockRepository repository;

    @InjectMocks
    private PageContentBlockService service;

    @Test
    void upsertUpdatesExistingPrimaryBlockInsteadOfCreatingDuplicates() {
        UUID pageId = UUID.randomUUID();
        PageContentBlock existing = new PageContentBlock();
        existing.setContentBlockId(UUID.randomUUID());
        existing.setPageId(pageId);
        existing.setType(ContentBlockType.text);
        existing.setContentData("{\"html\":\"old\"}");
        PageContentBlock incoming = new PageContentBlock();
        incoming.setContentData("{\"html\":\"new\"}");
        incoming.setLastModifiedByUserId(UUID.randomUUID());

        when(repository.findFirstByPageIdAndTypeOrderByOrderOnPageAsc(pageId, ContentBlockType.text))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        PageContentBlock saved = service.upsertPrimaryText(pageId, incoming);

        assertSame(existing, saved);
        assertEquals("{\"html\":\"new\"}", saved.getContentData());
        assertEquals(0, saved.getOrderOnPage());
        verify(repository).save(existing);
    }
}
