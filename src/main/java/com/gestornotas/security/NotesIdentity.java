package com.gestornotas.security;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class NotesIdentity {
    private NotesIdentity() {}
    public static UUID user(HttpServletRequest request) {
        Object value=request.getAttribute("notes.user-id");
        if(!(value instanceof UUID))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return (UUID)value;
    }
    public static void require(HttpServletRequest request,UUID owner) {
        if(!user(request).equals(owner))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
