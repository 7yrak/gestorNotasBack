package com.gestornotas.security;

import com.gestornotas.model.entity.User;
import com.gestornotas.repository.UserRepository;
import com.gestornotas.controller.NotebookController;
import com.gestornotas.model.entity.Notebook;
import com.gestornotas.service.NotebookService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalAccountTest {
    @Test void onlyConfiguredOwnerCanLoginAndWeakLegacyPasswordsAreRejected() {
        var users = mock(UserRepository.class);
        var id = UUID.randomUUID();
        var owner = new User();
        owner.setUsername("owner");
        owner.setPasswordHash("plaintext");
        when(users.findById(id)).thenReturn(Optional.of(owner));
        var account = new LocalAccount(users, id, "");
        assertThrows(UsernameNotFoundException.class, () -> account.loadUserByUsername("owner"));
        String hash = new BCryptPasswordEncoder().encode("local-test");
        var resetAccount = new LocalAccount(users, id, hash);
        var principal = (LocalAccount.Principal) resetAccount.loadUserByUsername("owner");
        assertEquals(id, principal.userId());
        assertTrue(new BCryptPasswordEncoder().matches("local-test", principal.getPassword()));
        assertThrows(UsernameNotFoundException.class, () -> resetAccount.loadUserByUsername("other"));
        when(users.findById(id)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> resetAccount.loadUserByUsername("owner"));
    }

    @Test void notebookCreationUsesTheAuthenticatedOwner() {
        var id = UUID.randomUUID();
        var request = new MockHttpServletRequest();
        request.setAttribute("notes.user-id", id);
        var body = new Notebook();
        body.setUserId(UUID.randomUUID());
        var service = mock(NotebookService.class);
        when(service.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals(id, new NotebookController(service).create(body, request).getBody().getUserId());
    }

    @Test void browserCannotChooseAnotherOwnersNotebooks() {
        var request = new MockHttpServletRequest();
        request.setAttribute("notes.user-id", UUID.randomUUID());
        var service = mock(NotebookService.class);
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> new NotebookController(service).getByUser(UUID.randomUUID(), request));
        verifyNoInteractions(service);
    }
}
