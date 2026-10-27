package com.gestornotas.controller;
import com.gestornotas.model.entity.Tag;
import com.gestornotas.service.TagService;
import com.gestornotas.security.NotesIdentity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/tags")
public class TagController {
    private final TagService service;
    public TagController(TagService service){this.service=service;}
    @GetMapping public ResponseEntity<List<Tag>> getAll(HttpServletRequest request){return ResponseEntity.ok(service.findAll().stream().filter(item->NotesIdentity.user(request).equals(item.getUserId())).toList());}
    @GetMapping("/{id}") public ResponseEntity<Tag> getById(@PathVariable UUID id,HttpServletRequest request){
        var item=service.findById(id);NotesIdentity.require(request,item.getUserId());return ResponseEntity.ok(item);
    }
    @PostMapping public ResponseEntity<Tag> create(@RequestBody Tag body,HttpServletRequest request){
        body.setUserId(NotesIdentity.user(request));return ResponseEntity.ok(service.save(body));
    }
    @PutMapping("/{id}") public ResponseEntity<Tag> update(@PathVariable UUID id,@RequestBody Tag body,HttpServletRequest request){
        NotesIdentity.require(request,service.findById(id).getUserId());body.setTagId(id);body.setUserId(NotesIdentity.user(request));return ResponseEntity.ok(service.save(body));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id,HttpServletRequest request){
        NotesIdentity.require(request,service.findById(id).getUserId());service.deleteById(id);return ResponseEntity.noContent().build();
    }
}
