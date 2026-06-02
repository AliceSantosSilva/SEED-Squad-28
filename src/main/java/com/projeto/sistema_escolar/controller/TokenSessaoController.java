package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.TokenSessao;
import com.projeto.sistema_escolar.service.TokenSessaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tokens")
public class TokenSessaoController {

    private final TokenSessaoService service;

    public TokenSessaoController(TokenSessaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TokenSessao> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TokenSessao> buscarPorId(@PathVariable Integer id) {
        Optional<TokenSessao> token = service.buscarPorId(id);
        return token.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<TokenSessao> buscarPorUsuario(@PathVariable Integer usuarioId) {
        return service.buscarPorUsuario(usuarioId);
    }

    @PostMapping
    public ResponseEntity<TokenSessao> criar(@Valid @RequestBody TokenSessao token) {
        TokenSessao saved = service.salvar(token);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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