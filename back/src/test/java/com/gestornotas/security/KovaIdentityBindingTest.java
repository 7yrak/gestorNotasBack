package com.gestornotas.security;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class KovaIdentityBindingTest {
 static final String INSTANCE="gn-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
 KovaIdentityBinding binding(UUID owner){return new KovaIdentityBinding(mock(JdbcTemplate.class),owner,INSTANCE,"jdbc:postgresql://127.0.0.1:5432/kova_gn_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");}
 @Test void customerOwnerAndAuthorizedAdministratorUseTheSameIsolatedNotesOwner(){UUID owner=UUID.randomUUID();var b=binding(owner);assertEquals(owner,b.require(owner.toString(),"OWNER"));assertEquals(owner,b.require(UUID.randomUUID().toString(),"ADMIN"));}
 @Test void anotherCustomerAndMalformedSubjectsCannotUseTheInstance(){var b=binding(UUID.randomUUID());assertThrows(org.springframework.web.server.ResponseStatusException.class,()->b.require(UUID.randomUUID().toString(),"OWNER"));assertThrows(org.springframework.web.server.ResponseStatusException.class,()->b.require("invalid","ADMIN"));}
 @Test void commercialProfileCannotConnectToThePersonalNotesDatabase(){assertThrows(IllegalArgumentException.class,()->new KovaIdentityBinding(mock(JdbcTemplate.class),UUID.randomUUID(),INSTANCE,"jdbc:postgresql://127.0.0.1:5432/gestor_notas"));}
}
