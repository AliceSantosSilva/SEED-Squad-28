package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Coordenador;
import com.projeto.sistema_escolar.service.CoordenadorService;
import com.projeto.sistema_escolar.service.EscolaService;
import com.projeto.sistema_escolar.util.EscolaFiltroUtil;
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
    private final EscolaService escolaService;
    private final EscolaFiltroUtil filtroUtil;

    public CoordenadorController(CoordenadorService service, EscolaService escolaService, EscolaFiltroUtil filtroUtil) {
        this.service = service;
        this.escolaService = escolaService;
        this.filtroUtil = filtroUtil;
    }

    @GetMapping
    public List<Coordenador> listar() {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            return service.listarPorEscola(escolaId);
        }
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
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (coordenador.getEscola() != null && coordenador.getEscola().getId() != null) {
            escolaService.buscarPorId(coordenador.getEscola().getId())
                .ifPresent(coordenador::setEscola);
        }
        Coordenador saved = service.salvar(coordenador);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coordenador> atualizar(@PathVariable Integer id, @Valid @RequestBody Coordenador coordAtualizado) {
        Optional<Coordenador> coordOpt = service.buscarPorId(id);
        if (coordOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Coordenador coord = coordOpt.get();
        coord.setArea(coordAtualizado.getArea());
        
        if (coordAtualizado.getEscola() != null && coordAtualizado.getEscola().getId() != null) {
            Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
            if (escolaId != null && !coordAtualizado.getEscola().getId().equals(escolaId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            coord.setEscola(coordAtualizado.getEscola());
        }
        coord.setUsuario(coordAtualizado.getUsuario());
        
        Coordenador saved = service.salvar(coord);
        return ResponseEntity.ok(saved);
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