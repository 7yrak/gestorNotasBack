package com.gestornotas.security;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
 "notes.kova.ui-uri=http://127.0.0.1:8082/",
 "spring.security.oauth2.client.registration.kova.client-id=gn-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
 "spring.security.oauth2.client.registration.kova.redirect-uri=http://127.0.0.1:8082/gestor-notas/login/oauth2/code/kova",
 "spring.security.oauth2.client.provider.kova.authorization-uri=http://127.0.0.1:18090/oauth2/authorize",
 "spring.security.oauth2.client.provider.kova.token-uri=http://127.0.0.1:18090/oauth2/token",
 "spring.security.oauth2.client.provider.kova.jwk-set-uri=http://127.0.0.1:18090/oauth2/jwks",
 "spring.security.oauth2.client.provider.kova.issuer-uri=http://127.0.0.1:18090","spring.datasource.url=jdbc:postgresql://127.0.0.1:15548/notes_customer","spring.datasource.username=kova_test","spring.datasource.password=${KOVA_NOTES_TEST_DB_PASSWORD}","notes.kova.owner-id=11111111-1111-1111-1111-111111111111"})
@AutoConfigureMockMvc @ActiveProfiles("kova-customer")
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named="KOVA_NOTES_TEST_DB_PASSWORD",matches=".+")
@org.springframework.transaction.annotation.Transactional
class CustomerSchemaTest {
 static HttpServer server;
 static volatile int code=200;
 static volatile String responseInstance="gn-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
 static volatile String role="OWNER";
 static final String ORG="11111111-1111-1111-1111-111111111111";
 @DynamicPropertySource static void properties(DynamicPropertyRegistry props) throws Exception {
  server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/check",exchange->{
   String body="{\"instanceId\":\""+responseInstance+"\",\"organizationId\":\""+ORG+
     "\",\"organizationName\":\"Pilot\",\"role\":\""+role+"\",\"status\":\"ACTIVE\"}";
   exchange.getResponseHeaders().set("Content-Type","application/json");
   exchange.sendResponseHeaders(code,body.getBytes().length);
   exchange.getResponseBody().write(body.getBytes()); exchange.close();
  });server.start();
  props.add("notes.kova.check-uri",()->"http://127.0.0.1:"+server.getAddress().getPort()+"/check");
 }
 @Autowired MockMvc mvc;
 @Autowired OAuth2AuthorizedClientService clients;
 @Autowired ClientRegistrationRepository registrations;
 @BeforeEach void reset() {code=200;role="OWNER";responseInstance="gn-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";}
 @AfterAll static void stop() {if(server!=null)server.stop(0);}
 private org.springframework.test.web.servlet.request.RequestPostProcessor signedIn(String org) {
  var login=oidcLogin().idToken(t->t.subject("11111111-1111-1111-1111-111111111111").claim("organization_id",org).claim("instance_id","gn-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
  var access=new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,"test-only-access",
      Instant.now(),Instant.now().plusSeconds(120));
  var principal=new org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser(
      List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
      new org.springframework.security.oauth2.core.oidc.OidcIdToken("test-id-token",Instant.now(),
      Instant.now().plusSeconds(120),Map.of("sub","11111111-1111-1111-1111-111111111111","organization_id",org,"instance_id","gn-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")));
  var auth=new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(principal,
      principal.getAuthorities(),"kova");
  clients.saveAuthorizedClient(new OAuth2AuthorizedClient(registrations.findByRegistrationId("kova"),
      "11111111-1111-1111-1111-111111111111",access),auth);
  return login.clientRegistration(registrations.findByRegistrationId("kova"));
 }

 @Autowired com.fasterxml.jackson.databind.ObjectMapper json;
 @Test void freshCustomerCanCreateAndReadNotebookSectionPageAndTextBlock() throws Exception {
  String notebook=mvc.perform(post("/api/notebooks").with(signedIn(ORG)).with(csrf()).contentType("application/json").content("{\"name\":\"Cliente\",\"userId\":\"22222222-2222-2222-2222-222222222222\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(ORG)).andReturn().getResponse().getContentAsString();
  String nid=json.readTree(notebook).get("notebookId").asText();
  String section=mvc.perform(post("/api/sections").with(signedIn(ORG)).with(csrf()).contentType("application/json").content("{\"name\":\"Trabajo\",\"notebookId\":\""+nid+"\"}"))
   .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  String sid=json.readTree(section).get("sectionId").asText();
  String page=mvc.perform(post("/api/pages").with(signedIn(ORG)).with(csrf()).contentType("application/json").content("{\"title\":\"Primera nota\",\"sectionId\":\""+sid+"\"}"))
   .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  String pid=json.readTree(page).get("pageId").asText();
  mvc.perform(post("/api/pageContentBlocks").with(signedIn(ORG)).with(csrf()).contentType("application/json").content(json.writeValueAsString(Map.of("pageId",pid,"type","text","contentData","{\"text\":\"Mis notas privadas\"}"))))
   .andExpect(status().isOk());
  mvc.perform(get("/api/notebooks/user/"+ORG).with(signedIn(ORG))).andExpect(status().isOk()).andExpect(jsonPath("$[0].notebookId").value(nid));
 }
 @Test void anotherCustomerCannotReadThisNotebookSpace() throws Exception {
  mvc.perform(get("/api/notebooks/user/22222222-2222-2222-2222-222222222222").with(signedIn(ORG))).andExpect(status().isForbidden());
 }
}
