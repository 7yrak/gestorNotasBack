package com.gestornotas.controller;
import com.gestornotas.model.entity.Notebook;
import com.gestornotas.service.NotebookService;
import com.gestornotas.security.NotesIdentity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/notebooks")
public class NotebookController {
    private final NotebookService service;
    public NotebookController(NotebookService service){this.service=service;}
    @GetMapping public ResponseEntity<List<Notebook>> getAll(HttpServletRequest request){return ResponseEntity.ok(service.findByUserId(NotesIdentity.user(request)));}
    @GetMapping("/{id}") public ResponseEntity<Notebook> getById(@PathVariable UUID id,HttpServletRequest request){
        var item=service.findById(id);NotesIdentity.require(request,item.getUserId());return ResponseEntity.ok(item);
    }
    @PostMapping public ResponseEntity<Notebook> create(@jakarta.validation.Valid @RequestBody Notebook body,HttpServletRequest request){
        body.setUserId(NotesIdentity.user(request));return ResponseEntity.ok(service.save(body));
    }
    @PutMapping("/{id}") public ResponseEntity<Notebook> update(@PathVariable UUID id,@RequestBody Notebook body,HttpServletRequest request){
        NotesIdentity.require(request,service.findById(id).getUserId());return ResponseEntity.ok(service.update(id,body));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id,HttpServletRequest request){
        NotesIdentity.require(request,service.findById(id).getUserId());service.deleteById(id);return ResponseEntity.noContent().build();
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notebook>> getByUser(@PathVariable UUID userId,HttpServletRequest request) {
        NotesIdentity.require(request,userId);return ResponseEntity.ok(service.findByUserId(userId));
    }
}
