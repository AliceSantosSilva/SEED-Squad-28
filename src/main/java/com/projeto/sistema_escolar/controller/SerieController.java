package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Serie;
import com.projeto.sistema_escolar.service.SerieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/series")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<Serie> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Serie criar(@RequestBody Serie serie) {
        return service.salvar(serie);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Serie> atualizar(@PathVariable Long id, @RequestBody Serie serieAtualizada) {
        return service.buscarPorId(id).map(serie -> {
            serie.setNome(serieAtualizada.getNome());
            serie.setNivelEnsino(serieAtualizada.getNivelEnsino());
            return ResponseEntity.ok(service.salvar(serie));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.existePorId(id)) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}