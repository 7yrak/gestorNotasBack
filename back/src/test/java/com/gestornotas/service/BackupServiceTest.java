package com.gestornotas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestornotas.model.entity.Notebook;
import com.gestornotas.model.entity.Page;
import com.gestornotas.model.entity.PageContentBlock;
import com.gestornotas.model.entity.Section;
import com.gestornotas.model.entity.SectionGroup;
import com.gestornotas.repository.NotebookRepository;
import com.gestornotas.repository.PageContentBlockRepository;
import com.gestornotas.repository.PageRepository;
import com.gestornotas.repository.ResourceRepository;
import com.gestornotas.repository.SectionGroupRepository;
import com.gestornotas.repository.SectionRepository;
import com.gestornotas.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {
    @Mock NotebookRepository notebookRepository;
    @Mock SectionGroupRepository sectionGroupRepository;
    @Mock SectionRepository sectionRepository;
    @Mock PageRepository pageRepository;
    @Mock PageContentBlockRepository contentBlockRepository;
    @Mock TagRepository tagRepository;
    @Mock ResourceRepository resourceRepository;

    private BackupService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new BackupService(notebookRepository, sectionGroupRepository, sectionRepository,
                pageRepository, contentBlockRepository, tagRepository, resourceRepository);
    }

    @Test
    void importsHierarchyWithFreshIdentifiers() throws Exception {
        UUID userId = UUID.randomUUID();
        Notebook existingNotebook = new Notebook();
        existingNotebook.setNotebookId(UUID.randomUUID());
        Section existingSection = new Section();
        existingSection.setSectionId(UUID.randomUUID());
        Page existingPage = new Page();
        existingPage.setPageId(UUID.randomUUID());
        PageContentBlock existingBlock = new PageContentBlock();
        existingBlock.setContentBlockId(UUID.randomUUID());

        when(notebookRepository.findByUserId(userId)).thenReturn(List.of(existingNotebook));
        when(sectionRepository.findByNotebookId(existingNotebook.getNotebookId())).thenReturn(List.of(existingSection));
        when(pageRepository.findBySectionId(existingSection.getSectionId())).thenReturn(List.of(existingPage));
        when(contentBlockRepository.findByPageIdOrderByOrderOnPageAsc(existingPage.getPageId())).thenReturn(List.of(existingBlock));
        when(notebookRepository.save(any())).thenAnswer(call -> withId(call.<Notebook>getArgument(0), UUID.randomUUID()));
        when(sectionRepository.save(any())).thenAnswer(call -> withId(call.<Section>getArgument(0), UUID.randomUUID()));
        when(pageRepository.save(any())).thenAnswer(call -> withId(call.<Page>getArgument(0), UUID.randomUUID()));
        when(contentBlockRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        UUID oldNotebook = UUID.randomUUID();
        UUID oldSection = UUID.randomUUID();
        UUID oldPage = UUID.randomUUID();
        JsonNode backup = objectMapper.readTree("""
                {
                  "format":"margen-backup",
                  "version":1,
                  "notebooks":[{
                    "notebookId":"%s",
                    "name":"Recuperado",
                    "sections":[{
                      "sectionId":"%s",
                      "name":"Ideas",
                      "pages":[{
                        "pageId":"%s",
                        "title":"Nota",
                        "contentBlocks":[{"type":"text","contentData":"{\\"html\\":\\"<p>Hola</p>\\"}","orderOnPage":0}]
                      }]
                    }]
                  }],
                  "tags":[],
                  "resources":[]
                }
                """.formatted(oldNotebook, oldSection, oldPage));

        BackupService.ImportResult result = service.importBackup(userId, backup);

        assertEquals(1, result.notebooks());
        assertEquals(1, result.sections());
        assertEquals(1, result.pages());
        assertEquals(1, result.contentBlocks());
        verify(contentBlockRepository).deleteAll(List.of(existingBlock));
        verify(pageRepository).deleteAll(List.of(existingPage));
        verify(sectionRepository).deleteAll(List.of(existingSection));
        verify(notebookRepository).deleteAll(List.of(existingNotebook));
        verify(contentBlockRepository).save(any(PageContentBlock.class));
    }

    @Test
    void rejectsUnknownBackupFormat() throws Exception {
        JsonNode backup = objectMapper.readTree("{\"format\":\"otro\",\"version\":1,\"notebooks\":[]}");
        assertThrows(ResponseStatusException.class, () -> service.importBackup(UUID.randomUUID(), backup));
    }

    private Notebook withId(Notebook entity, UUID id) {
        assertNotEquals(id, entity.getNotebookId());
        entity.setNotebookId(id);
        return entity;
    }

    private Section withId(Section entity, UUID id) {
        entity.setSectionId(id);
        return entity;
    }

    private Page withId(Page entity, UUID id) {
        entity.setPageId(id);
        return entity;
    }
}
