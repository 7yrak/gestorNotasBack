package com.gestornotas.security;

import com.gestornotas.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class LocalAccount implements UserDetailsService {
    private final UserRepository users;
    private final UUID ownerId;
    private final String passwordHash;

    public LocalAccount(UserRepository users,
            @Value("${notes.local.user-id}") UUID ownerId,
            @Value("${notes.local.password-hash:}") String passwordHash) {
        this.users = users;
        this.ownerId = ownerId;
        this.passwordHash = passwordHash;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var owner = users.findById(ownerId)
                .filter(user -> user.getUsername().equals(username))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
        String hash = passwordHash.isBlank() ? owner.getPasswordHash() : passwordHash;
        // Existing weak or plaintext credentials must be reset before local access.
        if (hash == null || !hash.matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}")) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }
        return new Principal(ownerId, owner.getUsername(), hash);
    }

    public static final class Principal extends org.springframework.security.core.userdetails.User {
        private final UUID userId;
        public Principal(UUID userId, String username, String hash) {
            super(username, hash, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
            this.userId = userId;
        }
        public UUID userId() { return userId; }
    }
}
