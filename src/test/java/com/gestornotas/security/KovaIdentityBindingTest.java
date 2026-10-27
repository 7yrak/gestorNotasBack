package com.gestornotas.security;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import com.gestornotas.controller.NotebookController;
import com.gestornotas.model.entity.Notebook;
import com.gestornotas.service.NotebookService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KovaIdentityBindingTest {
 @Test void preservedNotesUserIsResolvedFromTheKovaSubject(){
  var jdbc=mock(JdbcTemplate.class);UUID sub=UUID.randomUUID(),legacy=UUID.randomUUID();
  when(jdbc.queryForList(anyString(),eq(UUID.class),eq(sub))).thenReturn(List.of(legacy));
  assertEquals(legacy,new KovaIdentityBinding(jdbc).require(sub.toString()));
 }
 @Test void missingAndMalformedBindingsNeverFallBackToAnotherNotesUser(){
  var jdbc=mock(JdbcTemplate.class);UUID sub=UUID.randomUUID();
  when(jdbc.queryForList(anyString(),eq(UUID.class),eq(sub))).thenReturn(List.of());
  var binding=new KovaIdentityBinding(jdbc);
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->binding.require(sub.toString()));
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->binding.require("not-a-subject"));
 }
 @Test void notebookCreationIgnoresTheOwnerChosenByTheBrowser(){
  UUID legacy=UUID.randomUUID();var request=new MockHttpServletRequest();request.setAttribute("notes.user-id",legacy);
  var body=new Notebook();body.setUserId(UUID.randomUUID());body.setName("Existing account");
  var service=mock(NotebookService.class);when(service.save(any())).thenAnswer(invocation->invocation.getArgument(0));
  var result=new NotebookController(service).create(body,request);
  assertEquals(legacy,result.getBody().getUserId());verify(service).save(body);
 }
 @Test void browserCannotSelectAnotherUsersNotebooks(){
  var request=new MockHttpServletRequest();request.setAttribute("notes.user-id",UUID.randomUUID());
  var service=mock(NotebookService.class);
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->new NotebookController(service).getByUser(UUID.randomUUID(),request));
  verifyNoInteractions(service);
 }
}
