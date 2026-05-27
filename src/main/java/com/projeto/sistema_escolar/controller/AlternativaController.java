package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Alternativa;
import com.projeto.sistema_escolar.service.AlternativaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alternativas")
@CrossOrigin(origins = "*")
public class AlternativaController {

    private final AlternativaService service;

    public AlternativaController(AlternativaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Alternativa> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alternativa> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/questao/{questaoId}")
    public List<Alternativa> buscarPorQuestao(@PathVariable Long questaoId) {
        return service.buscarPorQuestao(questaoId);
    }

    @PostMapping
    public Alternativa criar(@RequestBody Alternativa alternativa) {
        return service.salvar(alternativa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alternativa> atualizar(@PathVariable Long id, @RequestBody Alternativa altAtualizada) {
        return service.buscarPorId(id).map(alt -> {
            alt.setTexto(altAtualizada.getTexto());
            alt.setCorreta(altAtualizada.isCorreta());
            alt.setQuestao(altAtualizada.getQuestao());
            return ResponseEntity.ok(service.salvar(alt));
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