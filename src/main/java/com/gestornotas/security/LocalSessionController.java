package com.gestornotas.security;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
public class LocalSessionController {
    @GetMapping("/auth/csrf")
    Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @GetMapping("/auth/info")
    Map<String, String> info() { return Map.of("provider", "LOCAL"); }

    @GetMapping("/auth/session")
    Map<String, Object> session(HttpServletRequest request, Authentication authentication) {
        return Map.of("legacyUserId", NotesIdentity.user(request), "username", authentication.getName());
    }
}
