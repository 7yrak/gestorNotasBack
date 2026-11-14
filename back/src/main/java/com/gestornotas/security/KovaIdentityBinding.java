package com.gestornotas.security;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service @Profile("kova-customer") @DependsOnDatabaseInitialization
public class KovaIdentityBinding {
 private final JdbcTemplate jdbc;private final UUID owner;private final String instance;
 public KovaIdentityBinding(JdbcTemplate jdbc,@Value("${notes.kova.owner-id}") UUID owner,
  @Value("${spring.security.oauth2.client.registration.kova.client-id}") String instance,
  @Value("${spring.datasource.url}") String database){
  if(!instance.matches("gn-[a-f0-9]{32}")||!database.matches("jdbc:postgresql://127\\.0\\.0\\.1:(5432/kova_gn_"+instance.substring(3)+"|155[0-4][0-9]/notes_customer)"))throw new IllegalArgumentException("An isolated Notes customer database is required");
  this.jdbc=jdbc;this.owner=owner;this.instance=instance;
 }
 @PostConstruct void initialize(){
  jdbc.update("insert into customer_identity(id,instance_id,owner_id) values (1,?,?) on conflict do nothing",instance,owner);
  if(!Boolean.TRUE.equals(jdbc.queryForObject("select instance_id=? and owner_id=? from customer_identity where id=1",Boolean.class,instance,owner)))throw new IllegalStateException("Customer database identity mismatch");
  jdbc.update("insert into users(user_id,username,email,password_hash) values (?,?,?,'!KOVA_ONLY') on conflict do nothing",owner,owner.toString(),owner+"@customer.invalid");
 }
 public UUID require(String subject,String role){
  UUID actor;try{actor=UUID.fromString(subject);}catch(Exception e){throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
  if(!("ADMIN".equals(role)||"OWNER".equals(role)&&owner.equals(actor)))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  return owner;
 }
}
