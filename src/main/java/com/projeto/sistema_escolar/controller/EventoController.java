package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.EventoDTO;
import com.projeto.sistema_escolar.model.Evento;
import com.projeto.sistema_escolar.service.EventoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public List<Evento> listar() {
        return eventoService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody EventoDTO dto, Authentication auth) {
        try {
            Evento evento = eventoService.criar(dto, auth.getName());
            return ResponseEntity.ok(evento);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"erro\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Integer id, Authentication auth) {
        try {
            eventoService.deletar(id, auth.getName());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"erro\":\"" + e.getMessage() + "\"}");
        }
    }
}