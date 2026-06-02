package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Coordenador;
import com.projeto.sistema_escolar.service.CoordenadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/coordenadores")
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
    public ResponseEntity<Coordenador> buscarPorId(@PathVariable Integer id) {
        Optional<Coordenador> coordenador = service.buscarPorId(id);
        return coordenador.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Coordenador> buscarPorUsuario(@PathVariable Integer usuarioId) {
        return service.buscarPorUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Coordenador> criar(@Valid @RequestBody Coordenador coordenador) {
        Coordenador saved = service.salvar(coordenador);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coordenador> atualizar(@PathVariable Integer id, @Valid @RequestBody Coordenador coordAtualizado) {
        return service.buscarPorId(id).map(coord -> {
            coord.setArea(coordAtualizado.getArea());
            coord.setEscola(coordAtualizado.getEscola());
            coord.setUsuario(coordAtualizado.getUsuario());
            return ResponseEntity.ok(service.salvar(coord));
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