package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.TokenSessao;
import com.projeto.sistema_escolar.service.TokenSessaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tokens")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<TokenSessao> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<TokenSessao> buscarPorUsuario(@PathVariable Long usuarioId) {
        return service.buscarPorUsuario(usuarioId);
    }

    @PostMapping
    public TokenSessao criar(@RequestBody TokenSessao token) {
        return service.salvar(token);
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