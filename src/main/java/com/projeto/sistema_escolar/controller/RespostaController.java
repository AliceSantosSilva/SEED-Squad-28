package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Resposta;
import com.projeto.sistema_escolar.service.RespostaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/respostas")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<Resposta> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/aluno/{alunoId}")
    public List<Resposta> buscarPorAluno(@PathVariable Long alunoId) {
        return service.buscarPorAluno(alunoId);
    }

    @GetMapping("/prova/{provaId}")
    public List<Resposta> buscarPorProva(@PathVariable Long provaId) {
        return service.buscarPorProva(provaId);
    }

    @PostMapping
    public Resposta criar(@RequestBody Resposta resposta) {
        return service.salvar(resposta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resposta> atualizar(@PathVariable Long id, @RequestBody Resposta respAtualizada) {
        return service.buscarPorId(id).map(resp -> {
            resp.setAlternativa(respAtualizada.getAlternativa());
            resp.setCorreta(respAtualizada.getCorreta());
            return ResponseEntity.ok(service.salvar(resp));
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