package com.gestornotas.controller;
import com.gestornotas.model.entity.Resource;
import com.gestornotas.service.ResourceService;
import com.gestornotas.security.NotesIdentity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/resources")
public class ResourceController {
    private final ResourceService service;
    public ResourceController(ResourceService service){this.service=service;}
    @GetMapping public ResponseEntity<List<Resource>> getAll(HttpServletRequest request){return ResponseEntity.ok(service.findAll().stream().filter(item->NotesIdentity.user(request).equals(item.getUserId())).toList());}
    @GetMapping("/{id}") public ResponseEntity<Resource> getById(@PathVariable UUID id,HttpServletRequest request){
        var item=service.findById(id);NotesIdentity.require(request,item.getUserId());return ResponseEntity.ok(item);
    }
    @PostMapping public ResponseEntity<Resource> create(@RequestBody Resource body,HttpServletRequest request){
        body.setUserId(NotesIdentity.user(request));return ResponseEntity.ok(service.save(body));
    }
    @PutMapping("/{id}") public ResponseEntity<Resource> update(@PathVariable UUID id,@RequestBody Resource body,HttpServletRequest request){
        NotesIdentity.require(request,service.findById(id).getUserId());body.setResourceId(id);body.setUserId(NotesIdentity.user(request));return ResponseEntity.ok(service.save(body));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id,HttpServletRequest request){
        NotesIdentity.require(request,service.findById(id).getUserId());service.deleteById(id);return ResponseEntity.noContent().build();
    }
}
