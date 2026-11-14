package com.gestornotas.service;

import com.gestornotas.model.entity.Page;
import com.gestornotas.repository.PageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageServiceTest {
    @Mock PageRepository repository;

    @Test
    void movesPageToAnotherSectionAndReordersBothLists() {
        PageService service = new PageService(repository);
        UUID sourceSection = UUID.randomUUID();
        UUID targetSection = UUID.randomUUID();
        Page first = page(sourceSection, 0);
        Page moved = page(sourceSection, 1);
        Page last = page(sourceSection, 2);
        Page target = page(targetSection, 0);

        when(repository.findById(moved.getPageId())).thenReturn(Optional.of(moved));
        when(repository.findBySectionIdAndIsDeletedFalseOrderByOrderInSectionAsc(sourceSection))
                .thenReturn(new ArrayList<>(List.of(first, moved, last)));
        when(repository.findBySectionIdAndIsDeletedFalseOrderByOrderInSectionAsc(targetSection))
                .thenReturn(new ArrayList<>(List.of(target)));

        Page result = service.move(moved.getPageId(), targetSection, 0);

        assertEquals(targetSection, result.getSectionId());
        assertEquals(0, result.getOrderInSection());
        assertEquals(0, first.getOrderInSection());
        assertEquals(1, last.getOrderInSection());
        assertEquals(1, target.getOrderInSection());
        verify(repository).flush();
    }

    private Page page(UUID sectionId, int order) {
        Page page = new Page();
        page.setPageId(UUID.randomUUID());
        page.setSectionId(sectionId);
        page.setOrderInSection(order);
        return page;
    }
}
