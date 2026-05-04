package com.courselylabs.courselylab.controller;

import com.courselylabs.courselylab.dto.CategoriaDTO;
import com.courselylabs.courselylab.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> findAll() {
        return ResponseEntity.ok(categoriaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoriaDTO> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoriaService.findBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<CategoriaDTO> create(@Valid @RequestBody CategoriaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<CategoriaDTO> update(@PathVariable Integer id, @Valid @RequestBody CategoriaDTO dto) {
        return ResponseEntity.ok(categoriaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
