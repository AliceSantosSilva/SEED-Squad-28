package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Escola;
import com.projeto.sistema_escolar.service.EscolaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/escolas")
public class EscolaController {

    private final EscolaService service;

    public EscolaController(EscolaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Escola> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Escola> buscarPorId(@PathVariable Integer id) {
        Optional<Escola> escola = service.buscarPorId(id);
        return escola.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Escola> criar(@Valid @RequestBody Escola escola) {
        Escola saved = service.salvar(escola);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Escola> atualizar(@PathVariable Integer id, @Valid @RequestBody Escola escolaAtualizada) {
        return service.buscarPorId(id).map(escola -> {
            escola.setNome(escolaAtualizada.getNome());
            escola.setEndereco(escolaAtualizada.getEndereco());
            escola.setCidade(escolaAtualizada.getCidade());
            escola.setEstado(escolaAtualizada.getEstado());
            escola.setCep(escolaAtualizada.getCep());
            escola.setEmail(escolaAtualizada.getEmail());
            escola.setTelefone(escolaAtualizada.getTelefone());
            return ResponseEntity.ok(service.salvar(escola));
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