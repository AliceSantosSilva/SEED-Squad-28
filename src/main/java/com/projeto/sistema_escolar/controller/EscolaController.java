package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Escola;
import com.projeto.sistema_escolar.service.EscolaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/escolas")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<Escola> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Escola criar(@RequestBody Escola escola) {
        return service.salvar(escola);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Escola> atualizar(@PathVariable Long id, @RequestBody Escola escolaAtualizada) {
        return service.buscarPorId(id).map(escola -> {
            escola.setNome(escolaAtualizada.getNome());
            escola.setEndereco(escolaAtualizada.getEndereco());
            escola.setCidade(escolaAtualizada.getCidade());
            escola.setEstado(escolaAtualizada.getEstado());
            escola.setCep(escolaAtualizada.getCep());
            return ResponseEntity.ok(service.salvar(escola));
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