package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Resposta;
import com.projeto.sistema_escolar.service.RespostaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/respostas")
public class RespostaController {

    private final RespostaService service;

    public RespostaController(RespostaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Resposta> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resposta> buscarPorId(@PathVariable Integer id) {
        Optional<Resposta> resposta = service.buscarPorId(id);
        return resposta.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/aluno/{alunoId}")
    public List<Resposta> buscarPorAluno(@PathVariable Integer alunoId) {
        return service.buscarPorAluno(alunoId);
    }

    @GetMapping("/prova/{provaId}")
    public List<Resposta> buscarPorProva(@PathVariable Integer provaId) {
        return service.buscarPorProva(provaId);
    }

    @PostMapping
    public ResponseEntity<Resposta> criar(@Valid @RequestBody Resposta resposta) {
        Resposta saved = service.salvar(resposta);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resposta> atualizar(@PathVariable Integer id, @Valid @RequestBody Resposta respAtualizada) {
        return service.buscarPorId(id).map(resp -> {
            resp.setAlternativa(respAtualizada.getAlternativa());
            resp.setCorreta(respAtualizada.getCorreta());
            return ResponseEntity.ok(service.salvar(resp));
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