package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Coordenador;
import com.projeto.sistema_escolar.service.CoordenadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/coordenadores")
@CrossOrigin(origins = "*")
public class CoordenadorController {

    private final CoordenadorService service;

    public CoordenadorController(CoordenadorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Coordenador> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coordenador> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Coordenador criar(@RequestBody Coordenador coordenador) {
        return service.salvar(coordenador);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coordenador> atualizar(@PathVariable Long id, @RequestBody Coordenador coordAtualizado) {
        return service.buscarPorId(id).map(coord -> {
            coord.setUsuario(coordAtualizado.getUsuario());
            coord.setArea(coordAtualizado.getArea());
            return ResponseEntity.ok(service.salvar(coord));
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