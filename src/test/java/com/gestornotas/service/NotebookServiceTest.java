package com.gestornotas.service;

import com.gestornotas.model.entity.Notebook;
import com.gestornotas.repository.NotebookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotebookServiceTest {

    @Mock
    private NotebookRepository repository;

    @InjectMocks
    private NotebookService service;

    @Test
    void deleteMarksNotebookAsDeletedWithoutRemovingIt() {
        UUID id = UUID.randomUUID();
        Notebook notebook = new Notebook();
        notebook.setNotebookId(id);
        notebook.setIsDeleted(false);
        when(repository.findById(id)).thenReturn(Optional.of(notebook));

        service.deleteById(id);

        assertTrue(notebook.getIsDeleted());
        verify(repository).save(notebook);
    }
}
