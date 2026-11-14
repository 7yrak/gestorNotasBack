package com.gestornotas.security;

import com.gestornotas.model.entity.User;
import com.gestornotas.repository.UserRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {LocalSessionController.class, LocalSecurityTest.Probe.class},
        properties = {"notes.local.user-id=11111111-1111-1111-1111-111111111111", "notes.local.password-hash="})
@Import({LocalSecurity.class, LocalAccount.class, LocalSessionController.class, LocalSecurityTest.Probe.class})
class LocalSecurityTest {
    private static final UUID OWNER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    @MockBean UserRepository users;
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;

    @RestController
    static class Probe {
        @GetMapping("/api/notebooks") Object read() { return List.of(); }
        @PostMapping("/api/notebooks") Object write() { return Map.of("ok", true); }
    }

    @BeforeEach void account() {
        var owner = new User();
        owner.setUserId(OWNER);
        owner.setUsername("owner");
        owner.setPasswordHash(encoder.encode("test-password"));
        when(users.findById(OWNER)).thenReturn(Optional.of(owner));
    }

    private MockHttpSession login() throws Exception {
        var result = mvc.perform(post("/auth/login").with(csrf())
                .param("username", "owner").param("password", "test-password"))
                .andExpect(status().isNoContent()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test void anonymousCannotReadPrivateRoutes() throws Exception {
        for (var path : List.of("/auth/session", "/api/notebooks", "/api/pages",
                "/api/sections", "/api/sectionGroups", "/api/resources", "/api/tags", "/api/users"))
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
    }

    @Test void passwordLoginPreservesOwnerAndCanReadNotes() throws Exception {
        var session = login();
        mvc.perform(get("/auth/session").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.legacyUserId").value(OWNER.toString()))
                .andExpect(jsonPath("$.username").value("owner"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(get("/api/notebooks").session(session)).andExpect(status().isOk());
    }

    @Test void wrongPasswordAndAnotherUsernameCannotLogin() throws Exception {
        mvc.perform(post("/auth/login").with(csrf()).param("username", "owner").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").with(csrf()).param("username", "another").param("password", "test-password"))
                .andExpect(status().isUnauthorized());
        verify(users, never()).findAll();
    }

    @Test void csrfProtectsLoginAndWrites() throws Exception {
        mvc.perform(post("/auth/login").param("username", "owner").param("password", "test-password"))
                .andExpect(status().isForbidden());
        var session = login();
        mvc.perform(post("/api/notebooks").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/api/notebooks").session(session).with(csrf())).andExpect(status().isOk());
        // A rejected write must not invalidate the authenticated session.
        mvc.perform(get("/auth/session").session(session)).andExpect(status().isOk());
    }

    @Test void userManagementAndLegacySsoAreUnavailable() throws Exception {
        var session = login();
        mvc.perform(get("/api/users").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/api/users").session(session).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get("/oauth2/authorization/kova").session(session)).andExpect(status().isNotFound());
        mvc.perform(get("/auth/info")).andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("LOCAL"));
    }

    @Test void logoutNeedsCsrfAndInvalidatesTheSession() throws Exception {
        var session = login();
        mvc.perform(post("/auth/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
        Assertions.assertTrue(session.isInvalid());
        mvc.perform(get("/auth/session")).andExpect(status().isUnauthorized());
    }

    @Test void realCsrfTokenCanBeUsedByBrowserForm() throws Exception {
        var result = mvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
        var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(result.getResponse().getContentAsString());
        var session = (MockHttpSession) result.getRequest().getSession(false);
        mvc.perform(post("/auth/login").session(session)
                .header(json.get("headerName").asText(), json.get("token").asText())
                .param("username", "owner").param("password", "test-password"))
                .andExpect(status().isNoContent());
    }
}
