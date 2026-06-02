package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Serie;
import com.projeto.sistema_escolar.service.SerieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/series")
public class SerieController {

    private final SerieService service;

    public SerieController(SerieService service) {
        this.service = service;
    }

    @GetMapping
    public List<Serie> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Serie> buscarPorId(@PathVariable Integer id) {
        Optional<Serie> serie = service.buscarPorId(id);
        return serie.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Serie> criar(@Valid @RequestBody Serie serie) {
        Serie saved = service.salvar(serie);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Serie> atualizar(@PathVariable Integer id, @Valid @RequestBody Serie serieAtualizada) {
        return service.buscarPorId(id).map(serie -> {
            serie.setNome(serieAtualizada.getNome());
            serie.setNivelEnsino(serieAtualizada.getNivelEnsino());
            return ResponseEntity.ok(service.salvar(serie));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (service.existePorId(id)) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}