package com.gestornotas.security;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KovaIdentityBinding {
    private final JdbcTemplate jdbc;
    public KovaIdentityBinding(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public UUID require(String subject) {
        UUID id;
        try{id=UUID.fromString(subject);}catch(IllegalArgumentException e){throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
        var users=jdbc.queryForList("select legacy_user_id from kova_identity_binding where kova_subject=?",UUID.class,id);
        if(users.size()!=1)throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return users.getFirst();
    }
}
